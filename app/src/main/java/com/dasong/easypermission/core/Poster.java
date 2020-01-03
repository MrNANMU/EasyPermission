package com.dasong.easypermission.core;

import android.app.Activity;
import android.text.TextUtils;

import com.dasong.easypermission.core.Annotations.BeforeRequest;
import com.dasong.easypermission.core.Annotations.PermissionDenied;
import com.dasong.easypermission.core.Annotations.PermissionDontAsk;
import com.dasong.easypermission.core.Annotations.PermissionGranted;
import com.dasong.easypermission.core.Annotations.RequestPermission;
import com.dasong.easypermission.example.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

public class Poster {

    public static final int MODE_LAZY = 0;
    public static final int MODE_REQUEST = 1;

    private int mode;
    private Activity activity;
    private int userRequestCode;
    private ParseThread parseThread;
    private PostThread postThread;
    private CountDownLatch lock;

    private Map<String, Method> parameterGrantedMethodMap; //有注解参数允许的方法列表
    private Map<String, Method> parameterDeniedMethodMap; //有注解参数拒绝的方法列表
    private Map<String, Method> parameterDontAskMethodMap;//有注解参数不再提示方法列表
    private Method parameterGrantedMethod; //无注解参数允许的方法
    private Method parameterDeniedMethod; //无注解参数拒绝的方法
    private Method parameterDontAskMethod;//无注解参数不再提示的方法

    public Poster(int mode, Activity activity) {
        this.mode = mode;
        this.activity = activity;
        this.lock = new CountDownLatch(1);

    }

    private void parse(){
        parseThread = new ParseThread();
        parseThread.start();
    }

    public void request(){

    }

    public void request(String...permissions){

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    public void clear(){
        activity = null;
    }

    private class ParseThread extends Thread {

        @Override
        public void run() {
            //解析
            //boolean error = false;
            try{
                Class clz = activity.getClass();
                checkHasUsedRequestPermission(clz);
                Method method = getBeforeRequest(clz);
                if (method != null) {
                    method.invoke(activity);
                }
                getPermissionDeniedCallback(clz);
                getPermissionGrantedCallback(clz);
                getPermissionDontAskCallback(clz);
            } catch (PermissionAnnotationException e) {
                e.printStackTrace();
                //error = true;
            }catch (IllegalAccessException e) {
                e.printStackTrace();
                //error = true;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                //error = true;
            } finally {
                lock.countDown();
                if(mode == MODE_REQUEST){
                    postThread = new PostThread();
                    postThread.start();
                }
            }

        }

        private void checkHasUsedRequestPermission(Class clz) throws PermissionAnnotationException{
            if(!clz.isAnnotationPresent(RequestPermission.class)){
                throw new PermissionAnnotationException("Annotation @RequestPermission not found");
            }
        }

        private Method getBeforeRequest(Class clz) {
            Method[] methods = clz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(BeforeRequest.class)) {
                    //多个方法使用该注解的话，只有一个会生效
                    return method;
                }
            }
            return null;
        }

        private void getPermissionDeniedCallback( Class clz) throws PermissionAnnotationException{
            Method[] methods = clz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(PermissionDenied.class)) {
                    PermissionDenied denied = method.getAnnotation(PermissionDenied.class);
                    String permission = denied.value();
                    Class<?>[] parameters = method.getParameterTypes();
                    if (TextUtils.isEmpty(permission)) {
                        //无注解参数、有单一方法参数的回调
                        if (parameters.length == 0) {
                            throw new PermissionAnnotationException("this method need method`s parameter or annotation`s parameter");
                        } else if (parameters.length > 1) {
                            LogUtils.e("this method`s parameters is too much,only need one");
                        } else {
                            if ("String[]".equals(parameters[0].getSimpleName())) {
                                //此形式不支持多个，仅第一个会生效
                                if (parameterDeniedMethod == null) parameterDeniedMethod = method;
                            }
                        }
                    } else {
                        //有注解参数的方法，不限制此方法是否有方法参数
                        if (parameterDeniedMethodMap == null)
                            parameterDeniedMethodMap = new HashMap<>();
                        parameterDeniedMethodMap.put(permission, method);
                    }
                }
            }
        }

        private void getPermissionGrantedCallback(Class clz) {
            Method[] methods = clz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(PermissionGranted.class)) {
                    PermissionGranted denied = method.getAnnotation(PermissionGranted.class);
                    String permission = denied.value();
                    Class<?>[] parameters = method.getParameterTypes();
                    if (TextUtils.isEmpty(permission)) {
                        //无注解参数、有单一方法参数的回调
                        if (parameters.length == 0) {
                            LogUtils.e("this method need method`s parameter or annotation`s parameter");
                        } else if (parameters.length > 1) {
                            LogUtils.e("this method`s parameters is too much,only need one");
                        } else {
                            if ("String[]".equals(parameters[0].getSimpleName())) {
                                //此形式不支持多个，仅第一个会生效
                                if (parameterGrantedMethod == null) parameterGrantedMethod = method;
                            }
                        }
                    } else {
                        //有注解参数的方法，不限制此方法是否有方法参数
                        if (parameterGrantedMethodMap == null)
                            parameterGrantedMethodMap = new HashMap<>();
                        parameterGrantedMethodMap.put(permission, method);
                    }
                }
            }
        }

        private void getPermissionDontAskCallback(Class clz) {
            Method[] methods = clz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(PermissionDontAsk.class)) {
                    PermissionDontAsk dontAsk = method.getAnnotation(PermissionDontAsk.class);
                    String permission = dontAsk.value();
                    Class<?>[] parameters = method.getParameterTypes();
                    if (TextUtils.isEmpty(permission)) {
                        //无注解参数、有单一方法参数的回调
                        if (parameters.length == 0) {
                            LogUtils.e("this method need method`s parameter or annotation`s parameter");
                        } else if (parameters.length > 1) {
                            LogUtils.e("this method`s parameters is too much,only need one");
                        } else {
                            if ("String[]".equals(parameters[0].getSimpleName())) {
                                //此形式不支持多个，仅第一个会生效
                                if (parameterDontAskMethod == null) parameterDontAskMethod = method;
                            }
                        }
                    } else {
                        //有注解参数的方法，不限制此方法是否有方法参数
                        if (parameterDontAskMethodMap == null)
                            parameterDontAskMethodMap = new HashMap<>();
                        parameterDontAskMethodMap.put(permission, method);
                    }
                }
            }
        }
    }

    private class PostThread extends Thread {

        @Override
        public void run() {
            try {
                lock.await();
                Class clz = activity.getClass();
                //阻塞等待初始化的完成
                request(clz);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void postError(){

        }

        public void postCallback(){

        }

        private void request(Class clz){
            RequestPermission requestPermissionUtil = (RequestPermission) clz.getAnnotation(RequestPermission.class);
            String[] permissionWantToRequest = requestPermissionUtil.value();
            userRequestCode = requestPermissionUtil.requestCode();
            if (permissionWantToRequest != null && permissionWantToRequest.length > 0) {
                final String[] disablePermissionArray = filter(permissionWantToRequest);
                if (disablePermissionArray != null && disablePermissionArray.length > 0) {
                    ActivityCompat.requestPermissions(activity, disablePermissionArray, userRequestCode);
                }
            }
        }

        private String[] filter(String[] base) {
            List<String> disablePermission = new LinkedList<>();
            for (String permission : base) {
                boolean disable = ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_DENIED;
                if (disable) {
                    disablePermission.add(permission);
                }
            }
            return disablePermission.toArray(new String[disablePermission.size()]);
        }
    }
}
