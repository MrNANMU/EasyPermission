package com.dasong.easypermission.core;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class EasyPermission {

    //重构一下

    private EasyPermission mEp;
    private Worker mWorker;

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
        getInstance().mWorker = new Worker(Worker.MODE_LAZY,activity);
    }

    public static void post(){

    }

    public static void post(String...permissions){

    }

    public static void request(Activity activity){
        getInstance().mWorker = new Worker(Worker.MODE_REQUEST,activity);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getInstance().mWorker.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public static void clear(){
        getInstance().mWorker.clear();
        getInstance().mWorker = null;
    }

}
