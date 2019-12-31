package com.dasong.easypermission.core;

import android.app.Activity;

public class Poster {

    public static final int MODE_LAZY = 0;
    public static final int MODE_REQUEST = 1;

    private int mode;
    private Activity activity;

    public Poster(int mode, Activity activity) {
        this.mode = mode;
        this.activity = activity;
    }

    public void request(){

    }

    public void request(String...permissions){

    }

    public void clear(){
        activity = null;
    }
}
