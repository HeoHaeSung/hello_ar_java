package com.google.ar.core.examples.java.helloar.rendering;

import android.content.Context;
import android.support.annotation.Nullable;



import org.rajawali3d.Object3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.OnObjectPickedListener;

/**
 * Created by rogin on 2018-02-05.
 */

public class TouchAndDragRenderer extends ObjectRenderer {

  private ObjectRenderer mSelectedObject;
private int[] mViewport;
    private double[] mNearPos4;
    private double[] mFarPos4;
    private Vector3 mNearPos;
    private Vector3 mFarPos;
    private Vector3 mNewObjPos;
    private Matrix4 mViewMatrix;
    private Matrix4 mProjectionMatrix;



}
