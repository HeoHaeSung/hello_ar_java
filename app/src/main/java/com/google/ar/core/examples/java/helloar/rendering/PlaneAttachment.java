package com.google.ar.core.examples.java.helloar.rendering;

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.examples.java.helloar.model.ObjectsModel;

import static com.google.ar.core.Trackable.TrackingState.TRACKING;

/**
 * Created by rogin on 2018-01-25.
 */

public class PlaneAttachment extends Anchor {
    private final Plane mPlane;
    private final Anchor mAnchor;
    private float mScaleFactor;
    private ObjectsModel mModel;
    private float[] mModelViewProjectionMatrix = new float[16];
    private final float[] mPoseTranslation = new float[3];
    private final float[] mPoseRotation = new float[4];
    public PlaneAttachment(Plane plane, Anchor anchor, ObjectsModel model, float scaleFactor) {
        mPlane = plane;
        mAnchor = anchor;
        mScaleFactor = scaleFactor;
        mModel = model;
    }
    public boolean isTracking() {
        return                 mPlane.getTrackingState() ==Plane.TrackingState.TRACKING &&
                        mAnchor.getTrackingState() == mAnchor.getTrackingState();
    }
    public Pose getPose() {
        Pose pose = mAnchor.getPose();
        pose.getTranslation(mPoseTranslation, 0);
        pose.getRotationQuaternion(mPoseRotation, 0);
        mPoseTranslation[1] = mPlane.getCenterPose().ty();
        return new Pose(mPoseTranslation, mPoseRotation);
    }
    public Anchor getAnchor() {
        return mAnchor;
    }
    public ObjectsModel getModel() { return mModel; }
    public float getScaleFactor() { return mScaleFactor; }
    public void setScaleFactor(float scaleFactor){
        this.mScaleFactor = scaleFactor;
    }
}
