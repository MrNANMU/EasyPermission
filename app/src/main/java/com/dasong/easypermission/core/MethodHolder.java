package com.dasong.easypermission.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class MethodHolder {

    private List<PostMethod> methods;

    public MethodHolder(){
        methods = new LinkedList<>();
    }

    public void push(String permissionName,Method method,Object...agrs){
        PostMethod postMethod = new PostMethod(permissionName,method,agrs);
        methods.add(postMethod);
    }



    public static class PostMethod{

        private String permissionName;
        private Method method;
        private Object[] args;

        public PostMethod(String permissionName, Method method, Object[] args) {
            this.permissionName = permissionName;
            this.method = method;
            this.args = args;
        }

        public void invoke(Object target){
            try {
                method.invoke(target,args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }

}
