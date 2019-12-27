package com.dasong.easypermission.core.old;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

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

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

public class Ep implements Runnable {

    private Activity activity;
    private int userRequestCode;
    private Thread initThread;
    private Callback callback;
    private boolean checkBaseAnnotation;
    private boolean initFinished = false;

    private Map<String, Method> parameterGrantedMethodMap; //有注解参数允许的方法列表
    private Map<String, Method> parameterDeniedMethodMap; //有注解参数拒绝的方法列表
    private Map<String, Method> parameterDontAskMethodMap;//有注解参数不再提示方法列表
    private Method parameterGrantedMethod; //无注解参数允许的方法
    private Method parameterDeniedMethod; //无注解参数拒绝的方法
    private Method parameterDontAskMethod;//无注解参数不再提示的方法

    public Ep(Activity activity, Callback callback, boolean checkBaseAnnotation) {
        this.activity = activity;
        this.callback = callback;
        this.checkBaseAnnotation = checkBaseAnnotation;
        userRequestCode = EasyPermission.DEFAULT_REQUEST_CODE;
        init();
    }

    private void init() {
        initThread = new Thread(this);
        initThread.start();
    }

    @Override
    public void run() {
        Class clz = activity.getClass();
        try {
            Method method = getBeforeRequest(clz);
            if (method != null) method.invoke(activity);
            getPermissionDeniedCallback(0, clz);
            getPermissionGrantedCallback(0, clz);
            getPermissionDontAskCallback(0, clz);
            if (hasUsedRequestPermission(clz)) {
                RequestPermission requestPermissionUtil = (RequestPermission) clz.getAnnotation(RequestPermission.class);
                String[] permissionWantToRequest = requestPermissionUtil.value();
                userRequestCode = requestPermissionUtil.requestCode();
                if (permissionWantToRequest != null && permissionWantToRequest.length > 0) {
                    final LinkedList<String> disablePermission = new LinkedList<>();
                    filter(permissionWantToRequest, disablePermission);
                    final String[] disablePermissionArray = disablePermission.toArray(new String[disablePermission.size()]);
                    if (disablePermissionArray != null && disablePermissionArray.length > 0) {
                        ActivityCompat.requestPermissions(activity, disablePermissionArray, userRequestCode);
                    }
                }
            }
            initFinished = true;
            callback.finish(userRequestCode);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public void request(int requestCode, String... permission) {
        if (permission == null || permission.length == 0) return;
        userRequestCode = requestCode;
        final LinkedList<String> disablePermission = new LinkedList<>();
        filter(permission, disablePermission);
        if (disablePermission.size() == 0) return;
        final String[] disablePermissionArray = disablePermission.toArray(new String[disablePermission.size()]);
        ActivityCompat.requestPermissions(activity, disablePermissionArray, userRequestCode);
    }

    public boolean epOf(Activity activity) {
        return this.activity.equals(activity);
    }

    public void clear() {
        if (initThread != null && initThread.isAlive() && !initThread.isInterrupted()) {
            initThread.interrupt();
        }

        activity = null;
        initThread = null;

        if (parameterGrantedMethodMap != null) {
            parameterGrantedMethodMap.clear();
            parameterGrantedMethodMap = null;
        }
        parameterGrantedMethod = null;

        if (parameterDeniedMethodMap != null) {
            parameterDeniedMethodMap.clear();
            parameterDeniedMethodMap = null;
        }
        parameterDeniedMethod = null;

        if (parameterDontAskMethodMap != null) {
            parameterDontAskMethodMap.clear();
            parameterDontAskMethodMap = null;
        }
        parameterDontAskMethod = null;

        callback = null;

    }

    private boolean hasUsedRequestPermission(Class clz) {
        if (checkBaseAnnotation && !clz.isAnnotationPresent(RequestPermission.class)) {
            throw new NullPointerException("this Activity don`t use the Annotation @RequestPermission");
        }
        return checkBaseAnnotation && true;
    }

    private boolean hasUsedCallbackAnnotation(Class clz) {
        if (clz == null) throw new NullPointerException("Class is Null");
        return !clz.isAnnotationPresent(PermissionGranted.class)
                && !clz.isAnnotationPresent(PermissionDenied.class)
                && !clz.isAnnotationPresent(PermissionDontAsk.class);
    }

    private void filter(String[] base, List<String> disablePermission) {
        for (String permission : base) {
            boolean disable = ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_DENIED;
            if (disable) {
                disablePermission.add(permission);
            }
        }
    }

    private static Method getBeforeRequest(Class clz) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(BeforeRequest.class)) {
                //多个方法使用该注解的话，只有一个会生效
                return method;
            }
        }
        return null;
    }

    private void getPermissionDeniedCallback(int which, Class clz) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PermissionDenied.class)) {
                PermissionDenied denied = method.getAnnotation(PermissionDenied.class);
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

    private void getPermissionGrantedCallback(int which, Class clz) {
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

    private void getPermissionDontAskCallback(int which, Class clz) {
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != userRequestCode) return;
        Class clz = activity.getClass();
        if (!hasUsedCallbackAnnotation(clz)) {
            return;
        }
        LinkedList<String> granteds = null;
        LinkedList<String> denieds = null;
        LinkedList<String> dontAsks = null;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PERMISSION_DENIED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    if (dontAsks == null) dontAsks = new LinkedList<>();
                    dontAsks.add(permissions[i]);
                } else {
                    if (denieds == null) denieds = new LinkedList<>();
                    denieds.add(permissions[i]);
                }
            } else {
                if (granteds == null) granteds = new LinkedList<>();
                granteds.add(permissions[i]);
            }
        }
        String[] deniedsArray = denieds == null ? new String[0] : denieds.toArray(new String[denieds.size()]);
        handDeniedMethods(deniedsArray);
        String[] grantedsArray = granteds == null ? new String[0] : granteds.toArray(new String[granteds.size()]);
        handGrantedMethods(grantedsArray);
        String[] dontAskArray = dontAsks == null ? new String[0] : dontAsks.toArray(new String[dontAsks.size()]);
        handDontAskMethods(dontAskArray);
    }

    private void handDontAskMethods(String[] dontAskPermissions) {
        if (dontAskPermissions == null || dontAskPermissions.length == 0) return;
        try {
            if (parameterDontAskMethod != null) {
                parameterDontAskMethod.invoke(activity, new Object[]{dontAskPermissions});
            }
            if (parameterDontAskMethodMap != null && parameterDontAskMethodMap.size() > 0) {
                for (String permission : dontAskPermissions) {
                    Method method = parameterDontAskMethodMap.get(permission);
                    if (method != null) method.invoke(activity);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void handDeniedMethods(String[] deniedPermissions) {
        if (deniedPermissions == null || deniedPermissions.length == 0) return;
        try {
            if (parameterDeniedMethod != null) {
                parameterDeniedMethod.invoke(activity, new Object[]{deniedPermissions});
            }
            if (parameterDeniedMethodMap != null && parameterDeniedMethodMap.size() > 0) {
                for (String permission : deniedPermissions) {
                    Method method = parameterDeniedMethodMap.get(permission);
                    if (method != null) method.invoke(activity);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void handGrantedMethods(String[] grantedPermissions) {
        if (grantedPermissions == null || grantedPermissions.length == 0) return;
        try {
            if (parameterGrantedMethod != null) {
                parameterGrantedMethod.invoke(activity, new Object[]{grantedPermissions});
            }
            if (parameterGrantedMethodMap != null && parameterGrantedMethodMap.size() > 0) {
                for (String permission : grantedPermissions) {
                    Method method = parameterGrantedMethodMap.get(permission);
                    if (method != null) method.invoke(activity);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface Callback {
        void finish(int requestCode);
    }
}
