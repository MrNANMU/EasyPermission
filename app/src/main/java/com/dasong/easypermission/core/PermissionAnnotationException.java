package com.dasong.easypermission.core;

public class PermissionAnnotationException extends Exception{

    public PermissionAnnotationException() {
    }

    public PermissionAnnotationException(String message) {
        super(message);
    }

    public PermissionAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionAnnotationException(Throwable cause) {
        super(cause);
    }

    public PermissionAnnotationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
