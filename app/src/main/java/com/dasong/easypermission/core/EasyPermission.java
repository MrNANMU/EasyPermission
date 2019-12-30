package com.dasong.easypermission.core;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.collection.ArrayMap;

import java.util.Map;

public class EasyPermission {

    private EasyPermission mEp;
    private Poster mPoster;

    private EasyPermission(){

    }

    private static EasyPermission getInstance(){
        return EPInstance.instance;
    }

    private static class EPInstance{
        private static final EasyPermission instance = new EasyPermission();
    }

    /**
     * {@link #init(Activity)}总是和{@link #post()}或{@link #post(String...)}配合使用。
     * 如果使用了{@link #init(Activity)}来初始化，则{@link #request(Activity)}就会抛出异常。
     * 如果没有使用本方法初始化就调用了{@link #post()}或{@link #post(String...)}，同样会抛出异常
     * @param activity
     */
    public static void init(Activity activity){
        getInstance().mPoster = new Poster(Poster.MODE_LASY,activity);
    }

    public static void post(){

    }

    public static void post(String...permissions){

    }

    public static void request(Activity activity){
        getInstance().mPoster = new Poster(Poster.MODE_REQUEST,activity);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    public static void clear(Activity activity){

    }
}
