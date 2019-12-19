package com.dasong.easypermission.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface RequestPermission {

    String[] value();

    int requestCode() default EasyPermission.DEFAULT_REQUEST_CODE;
}
