package com.dasong.easypermission.core;

import android.app.Activity;

import androidx.collection.ArrayMap;

import java.util.Map;

public class EasyPermission {

    private static Map<String,Poster> cache;

    /**
     * {@link #init(Activity)}总是和{@link #post()}或{@link #post(String...)}配合使用。
     * 如果使用了{@link #init(Activity)}来初始化，则{@link #request(Activity)}就会抛出异常。
     * 如果没有使用本方法初始化就调用了{@link #post()}或{@link #post(String...)}，同样会抛出异常
     * @param activity
     */
    public static void init(Activity activity){
        if(cache == null){
            cache = new ArrayMap<>();
        }
        if(cache.get(activity.getClass().getName()) == null){
            Poster poster = new Poster(Poster.MODE_LASY,activity);
            cache.put(activity.getClass().getSimpleName(),poster);
        }
    }

    public static void post(Activity activity){
        Poster poster = cache.get(activity.getClass().getName());
        if(poster != null){
            poster.request();
        }
    }

    public static void post(Activity activity,String...permissions){
        Poster poster = cache.get(activity.getClass().getName());
        if(poster != null){
            poster.request(permissions);
        }
    }

    public static void request(Activity activity){
        if(cache == null){
            cache = new ArrayMap<>();
        }
        Poster poster = new Poster(Poster.MODE_LASY,activity);
        poster.request();
    }

    public static void clear(Activity activity){
        Poster poster = cache.get(activity.getClass().getName());
        if(poster != null){
            poster.clear();
        }
    }
}
