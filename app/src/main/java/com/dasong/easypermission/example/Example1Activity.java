package com.dasong.easypermission.example;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dasong.easypermission.core.Annotations.BeforeRequest;
import com.dasong.easypermission.core.old.EasyPermission;
import com.dasong.easypermission.core.Annotations.PermissionDenied;
import com.dasong.easypermission.core.Annotations.PermissionDontAsk;
import com.dasong.easypermission.core.Annotations.PermissionGranted;
import com.dasong.easypermission.core.Annotations.RequestPermission;


@RequestPermission({Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA})
public class Example1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasyPermission.init(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @BeforeRequest
    public void before(){
        LogUtils.e("请求之前调用");
    }

    @PermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void granted(){
        LogUtils.e("同意权限");
    }

    @PermissionDenied
    public void denied(String[] permissions){
        LogUtils.e("拒绝了"+permissions.length+"个权限");
    }

    @PermissionDontAsk(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void dontAsk(){
        LogUtils.e("不再提醒");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EasyPermission.clear();
    }
}
