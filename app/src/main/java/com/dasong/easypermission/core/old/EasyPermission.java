package com.dasong.easypermission.core.old;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class EasyPermission {

    public static final int DEFAULT_REQUEST_CODE = 1075;
    private static volatile boolean hasInited = false;
    private static Ep sCurrentEp;
    private static Ep.Callback callback;

    public static void init(Activity activity) {
        callback = new Ep.Callback() {
            @Override
            public void finish(int requestCode) {
                hasInited = true;
            }
        };
        init(activity,callback,true);
    }

    public static void init(Activity activity,boolean checkBaseAnnotation) {
        callback = new Ep.Callback() {
            @Override
            public void finish(int requestCode) {
                hasInited = true;
            }
        };
        init(activity,callback,checkBaseAnnotation);
    }

    public static void init(Activity activity,Ep.Callback callback) {
        init(activity,callback,true);
    }

    public static void init(Activity activity,Ep.Callback callback,boolean checkBaseAnnotation) {
        if (activity == null) {
            throw new NullPointerException("Activity can not be null!");
        }
        sCurrentEp = new Ep(activity,callback,checkBaseAnnotation);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(sCurrentEp == null) return;
        sCurrentEp.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public static void request(Activity activity, int requestCode,final String...permission){
        if(sCurrentEp != null && sCurrentEp.epOf(activity)){
            sCurrentEp.request(requestCode,permission);
        }else{
            init(activity, new Ep.Callback() {
                @Override
                public void finish(int requestCode) {
                    hasInited = true;
                    sCurrentEp.request(requestCode,permission);
                }
            });
        }
    }

    public static void request(Activity activity,final String...permission){
        request(activity,DEFAULT_REQUEST_CODE,permission);
    }

    public static void clear() {
        if(sCurrentEp != null) sCurrentEp.clear();
    }
}
