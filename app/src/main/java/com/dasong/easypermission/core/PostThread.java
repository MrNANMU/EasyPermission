package com.dasong.easypermission.core;

import java.util.concurrent.CountDownLatch;

class PostThread extends Thread {

    private CountDownLatch lock;

    public void bindLock(CountDownLatch lock){
        this.lock = lock;
    }
}
