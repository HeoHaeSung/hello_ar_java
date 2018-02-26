/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.Trackable.TrackingState;
import com.google.ar.core.examples.java.helloar.adapter.ModelSelectorAdapter;
import com.google.ar.core.examples.java.helloar.model.ObjectsModel;
import com.google.ar.core.examples.java.helloar.rendering.BackgroundRenderer;
import com.google.ar.core.examples.java.helloar.rendering.ObjectRenderer;
import com.google.ar.core.examples.java.helloar.rendering.PlaneAttachment;
import com.google.ar.core.examples.java.helloar.rendering.PlaneRenderer;
import com.google.ar.core.examples.java.helloar.rendering.PointCloudRenderer;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class HelloArActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private static final String TAG = HelloArActivity.class.getSimpleName();

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView mSurfaceView;
    private Session mSession;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private Snackbar mMessageSnackbar;
    private RotationGestureDetector mRotation;
    private float rotationAngle = 0f;
    private int mSelected = -1;
    private DisplayRotationHelper mDisplayRotationHelper;
    private final BackgroundRenderer mBackgroundRenderer = new BackgroundRenderer();
    private final ObjectRenderer[] mVirtualObject = new ObjectRenderer[mModelsInfo.length];
    private ObjectsModel[] mModels = new ObjectsModel[mModelsInfo.length];
    private ObjectsModel mCurrentSelectedModel;
    private float mCurrentScaleFactor = 1.0F;
    private static final float mModelScaleFactorChange = 0.03f;
    private final PlaneRenderer mPlaneRenderer = new PlaneRenderer();
    private final PointCloudRenderer mPointCloud = new PointCloudRenderer();
    private static final String[][] mModelsInfo = new String[][]{
            new String[]{"Android", "andy.obj", "andy.png", "1.0", "andy_preview.png"},
            new String[]{"warmashine", "war.obj", "war.png", "0.5", "war_pre.png"},
            new String[]{"Tauros", "Igor.obj", "igor.png", "0.3", "ii.PNG"},
            new String[]{"WoodCabin", "WoodenCabinObj.obj", "WoodCabinDif.png", "0.01", "wooden.PNG"},
            new String[]{"ironman", "m7.obj", "mk7.png", "0.5", "iron.png"},
            new String[]{"cat", "cat.obj", "cat.png", "0.4", "cat_preview.png"},
    };
    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] mAnchorMatrix = new float[16];
    // Tap handling and UI.
    private ArrayBlockingQueue<MotionEvent> mQueuedSingleTaps = new ArrayBlockingQueue<>(20);
    private ArrayBlockingQueue<MotionEvent> mQueuedMoveTaps = new ArrayBlockingQueue<>(Short.MAX_VALUE);
    private final ArrayList<PlaneAttachment> mTouches = new ArrayList<>();

    private final float CLICK_RANGE = ARApp.dpToPx(10/*dp*/);
    private float mclickx = 0;
    private float mclicky = 0;
    private float clickX = 0;
    private float clickY = 0;
    private int filter = 0;
    private float a,b;
    private boolean isClick = false;
private ImageView cc;
private ImageView shareIcon;
    private ImageView resetIcon;
    private ImageView undoIcon;
    private ImageView slideUpArrow;
    private ImageView cancelCross;
    private RelativeLayout slideView;
    private SlideUp slideUp;
    private DiscreteScrollView scrollView;
   private ImageView rr;
   private FrameLayout container;
    private boolean needSpaceShareImage = false;
    private Bitmap spaceShareImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surfaceview);
        container = (FrameLayout)findViewById(R.id.main);
        mDisplayRotationHelper = new DisplayRotationHelper(/*context=*/ this);
        for (int i = 0; i < mModelsInfo.length; i++) {
            String[] modelInfo = mModelsInfo[i];
            Bitmap previewImg = getBitmapFromAsset(this, modelInfo[4]);
            ObjectsModel obj = new ObjectsModel(modelInfo[0], modelInfo[1], modelInfo[2], Float.parseFloat(modelInfo[3]));
            obj.setPreview(previewImg);
            mCurrentSelectedModel = obj;
            mModels[i] = obj;
        }
        shareIcon = (ImageView)findViewById(R.id.shareIcon);
        container = (FrameLayout) findViewById(R.id.main);
        cc = (ImageView)findViewById(R.id.capture);
        rr = (ImageView)findViewById(R.id.rr);
        resetIcon = (ImageView) findViewById(R.id.resetIcon);
        undoIcon = (ImageView) findViewById(R.id.undoIcon);
        scrollView = findViewById(R.id.picker);
        cancelCross = (ImageView) findViewById(R.id.cancelModelPicker);
        slideUpArrow = (ImageView) findViewById(R.id.slideUpArrow);
        slideView = (RelativeLayout) findViewById(R.id.slideView);
        slideUp = new SlideUpBuilder(slideView)
                .withStartState(SlideUp.State.HIDDEN)
                .withStartGravity(Gravity.BOTTOM)
                .withLoggingEnabled(true)
                .build();
        scrollView.setAdapter(new ModelSelectorAdapter(mModels, this));
        resetIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                resetObjects();
            }
        });
        undoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undoObject();
            }
        });
        slideUpArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.show();
                slideView.requestLayout();
                slideView.requestFocus();
                scrollView.requestLayout();
                scrollView.requestFocus();
                slideUp.show();
                scrollView.smoothScrollToPosition(1);
                slideUpArrow.setVisibility(View.INVISIBLE);
            }
        });
        cc.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {

                                  }
                              });

        cancelCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.hideImmediately();
                slideUpArrow.setVisibility(View.VISIBLE);
            }
        });
        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                needSpaceShareImage = true;
            }
        });

        // Set up tap listener.
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onSingleTap(e);
                return true;
           }
            @Override
            public boolean onDown(MotionEvent e) {return true;}
        });
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isClick = true;
                        clickX = event.getX();
                        clickY = event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if ((filter % 2) == 0) {
                            mQueuedMoveTaps.offer(event);
                        } else if (filter > 1000) {
                            filter = 0;
                        } else {
                            filter++;
                        }
                        break;
                    case MotionEvent.ACTION_UP:

                        mclickx = event.getX();
                        mclicky = event.getY();
                         a = mclickx - clickX;
                         b = mclicky - clickY;
                        if(isClick && Math.abs(a) != CLICK_RANGE && Math.abs(b) != CLICK_RANGE )
                        {
                            mQueuedSingleTaps.offer(event);
                        }
                        mQueuedMoveTaps.clear();
                        isClick = false;
                            break;
                }
                mScaleGestureDetector.onTouchEvent(event);
                return false;
//                return mGestureDetector.onTouchEvent(event);
            }
        });

/*
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleGestureDetector.onTouchEvent(event);
                return mGestureDetector.onTouchEvent(event);
            }
        });
        */
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                if (scaleFactor > 1) {
                    mCurrentScaleFactor += mCurrentScaleFactor * mModelScaleFactorChange * scaleFactor * scaleFactor;
                } else {
                    mCurrentScaleFactor -= mCurrentScaleFactor * mModelScaleFactorChange * scaleFactor * scaleFactor;
                }
                return true;
            }
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        });


        // Set up renderer.
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        Exception exception = null;
        String message = null;
        try {
            mSession = new Session(/* context= */ this);
        } catch (UnavailableArcoreNotInstalledException e) {
            message = "Please install ARCore";
            exception = e;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app";
            exception = e;
        } catch (Exception e) {
            message = "This device does not support AR";
            exception = e;
        }
        if (message != null) {
            showSnackbarMessage(message, true);
            Log.e(TAG, "Exception creating session", exception);
            return;
        }
        Config config = new Config(mSession);
        if (!mSession.isSupported(config)) {
            showSnackbarMessage("This device does not support AR", true);
        }
        mSession.configure(config);
    }
    private Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();
        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }
        return bitmap;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (CameraPermissionHelper.hasCameraPermission(this)) {
            if (mSession != null) {
                showLoadingMessage();
                // Note that order matters - see the note in onPause(), the reverse applies here.
                mSession.resume();
            }
            mSurfaceView.onResume();
            mDisplayRotationHelper.onResume();
            resetObjects();
        } else {
            CameraPermissionHelper.requestCameraPermission(this);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        mDisplayRotationHelper.onPause();
        mSurfaceView.onPause();
        if (mSession != null) {
            mSession.pause();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this,
                    "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    private void onSingleTap(MotionEvent e){
        // Queue tap if there is space. Tap is lost if queue is full.
        mQueuedSingleTaps.offer(e);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        // Create the texture and pass it to ARCore session to be filled during update().
        mBackgroundRenderer.createOnGlThread(/*context=*/ this);
        if (mSession != null) {
            mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
        }
        // Prepare the other rendering objects.
        try {
            for (int i = 0; i < mVirtualObject.length; i++) {
                mVirtualObject[i] = new ObjectRenderer(mModels[i]);
                mVirtualObject[i].createOnGlThread(/*context=*/this, mModels[i].getObjectFileName(), mModels[i].getDiffuseTextureFileName());
                mVirtualObject[i].setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
        try {
            mPlaneRenderer.createOnGlThread(/*context=*/this, "trigrid.png");
        } catch (IOException e) {
            Log.e(TAG, "Failed to read plane texture");
        }
        mPointCloud.createOnGlThread(/*context=*/this);
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDisplayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mSession == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        mDisplayRotationHelper.updateSessionIfNeeded(mSession);
        try {
            Frame frame = mSession.update();
            Camera camera = frame.getCamera();
            MotionEvent tap = mQueuedSingleTaps.poll();
            if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(tap)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon
                    Trackable trackable = hit.getTrackable();
                    if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                        // Cap the number of objects created. This avoids overloading both the
                        // rendering system and ARCore.
                        if (mTouches.size() >= 20) {
                            mTouches.get(0).getAnchor();
                            mTouches.remove(0);
                            //mShowingTapPointX.remove(0);
                            //mShowingTapPointY.remove(0);
                        }
                        PlaneAttachment planeAttachment = new PlaneAttachment(
                                ((Plane) trackable),
                                mSession.createAnchor(hit.getHitPose()),
                                mCurrentSelectedModel,
                                mCurrentSelectedModel.getScaleFactor()
                        );
                        mTouches.add(planeAttachment);
                        //mShowingTapPointX.add(tap.getX());
                        //mShowingTapPointY.add(tap.getY());
                        // nowTouchingPointIndex = mTouches.size() - 1;
                        // Hits are sorted by depth. Consider only closest hit on a plane.
                        break;
                    }
                }
            }
            MotionEvent event = mQueuedMoveTaps.poll();
            if (event != null && camera.getTrackingState() == TrackingState.TRACKING && mTouches.size() > 0) {
                for (HitResult hit : frame.hitTest(event)) {
                    Trackable trackable = hit.getTrackable();
                    if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                        PlaneAttachment planeAttachment = new PlaneAttachment(((Plane) trackable),
                                mSession.createAnchor(hit.getHitPose()),
                                mCurrentSelectedModel,
                                mCurrentSelectedModel.getScaleFactor()
                        );
                        mTouches.get(mTouches.size() - 1).getAnchor();
                        mTouches.remove(mTouches.size() - 1);
                        mTouches.add(planeAttachment);
                        break;
                    }
        }
    }
            // Draw background.
                mBackgroundRenderer.draw(frame);
                // If not tracking, don't draw 3d objects.
                if (camera.getTrackingState() == TrackingState.PAUSED) {
                    return;
                }
                // Get projection matrix.
                float[] projmtx = new float[16];
                camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
                // Get camera matrix and draw.
                float[] viewmtx = new float[16];
                camera.getViewMatrix(viewmtx, 0);
                // Compute lighting from average intensity of the image.
                final float lightIntensity = frame.getLightEstimate().getPixelIntensity();
                // Visualize tracked points.
                PointCloud pointCloud = frame.acquirePointCloud();
                mPointCloud.update(pointCloud);
                mPointCloud.draw(viewmtx, projmtx);
                // Application is responsible for releasing the point cloud resources after
                // using it.
                pointCloud.release();
                // Check if we detected at least one plane. If so, hide the loading message.
                if (mMessageSnackbar != null) {
                    for (Plane plane : mSession.getAllTrackables(Plane.class)) {
                        if (plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING
                                && plane.getTrackingState() == TrackingState.TRACKING) {
                            hideLoadingMessage();
                            break;
                        }
                    }
                }
                // Visualize planes.
                mPlaneRenderer.drawPlanes(
                        mSession.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);
                // Visualize anchors created by touch.float scaleFactor = 1.0f;
                for (int i = 0; i < mTouches.size(); i++) {
                    PlaneAttachment touchAttachment = mTouches.get(i);
                    if (!touchAttachment.isTracking()) {
                        continue;
                    }
                    // Get the current pose of an Anchor in world space. The Anchor pose is updated
                    // during calls to session.update() as ARCore refines its estimate of the world.
                    touchAttachment.getPose().toMatrix(mAnchorMatrix, 0);
                    if (i == mTouches.size() - 1 && touchAttachment.getModel().getName().equals(mCurrentSelectedModel.getName()))
                        touchAttachment.setScaleFactor((mCurrentScaleFactor));

                    // Update and draw the model and its shadow.
                        for (ObjectRenderer virutalObject : mVirtualObject) {
                            if (virutalObject.getModel().getName().equals(touchAttachment.getModel().getName())) {
                                virutalObject.updateModelMatrix(mAnchorMatrix, touchAttachment.getScaleFactor() );
                                virutalObject.draw(viewmtx, projmtx, lightIntensity);
                                break;
                            }
                        }
                    }
            if(needSpaceShareImage){
                needSpaceShareImage = false;
                spaceShareImage = saveTexture(mSurfaceView.getWidth(), mSurfaceView.getHeight());
                Intent shareIntent = new Intent();
                shareIntent.setType("image/jpeg");
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM,getImageUri(this, spaceShareImage));

                startActivity(Intent.createChooser(shareIntent, "Share With"));
            }

            } catch(Throwable t){
                // Avoid crashing the application due to unhandled exceptions.
                Log.e(TAG, "Exception on the OpenGL thread", t);
            }
        }
    public static Bitmap saveTexture(int width, int height) {
        ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        reverseBuffer(buffer, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }
    private static void reverseBuffer(ByteBuffer buf, int width, int height)
    {
        int i = 0;
        byte[] tmp = new byte[width * 4];
        while (i++ < height / 2)
        {
            buf.get(tmp);
            System.arraycopy(buf.array(), buf.limit() - buf.position(), buf.array(), buf.position() - width * 4, width * 4);
            System.arraycopy(tmp, 0, buf.array(), buf.limit() - buf.position(), width * 4);
        }
        buf.rewind();
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private void showSnackbarMessage(String message, boolean finishOnDismiss) {
        mMessageSnackbar = Snackbar.make(
                HelloArActivity.this.findViewById(android.R.id.content),
                message, Snackbar.LENGTH_INDEFINITE);
        mMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        if (finishOnDismiss) {
            mMessageSnackbar.setAction(
                    "Dismiss",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMessageSnackbar.dismiss();
                        }
                    });
            mMessageSnackbar.addCallback(
                    new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            finish();}
                    });
        }
        mMessageSnackbar.show();
    }
    public void onClickModel(ObjectsModel obj, int position) {
        mCurrentSelectedModel = obj;
        mCurrentScaleFactor = obj.getScaleFactor();
    }
    private void showLoadingMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showSnackbarMessage("Searching for surfaces...", false);
            }
        });
    }
    private void undoObject() {
        if (mTouches.size() <= 0) return;
        mTouches.remove(mTouches.size() - 1);
    }
    private void resetObjects() {
        if (mTouches.size() <= 0) return;
        mTouches.clear();
    }
    private void hideLoadingMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMessageSnackbar != null) {
                    mMessageSnackbar.dismiss();
                }
                mMessageSnackbar = null;
            }
        });
    }
}
