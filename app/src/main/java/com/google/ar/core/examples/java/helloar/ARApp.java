package com.google.ar.core.examples.java.helloar;

import android.app.Application;

/**
 * Created by rogin on 2018-02-13.
 */

public class ARApp extends Application{
    public static float dsnsity;
    @Override
    public void onCreate(){
        super.onCreate();
        dsnsity = getResources().getDisplayMetrics().density;
    }
    public static int dpToPx(int dp){
        return (int) (( dp * dsnsity ) + 0.5f );
    }
}
