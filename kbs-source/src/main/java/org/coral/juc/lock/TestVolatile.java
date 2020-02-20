package org.coral.juc.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestVolatile {
    private volatile int vInt;
    private int nInt;
    private ReentrantLock reentrantLock = new ReentrantLock();

    public static void main(String[] args) {
        TestVolatile testVolatile = new TestVolatile();
        testVolatile.addVolatile();
        testVolatile.addNormal();
        testVolatile.addLock();
    }
    public void  addVolatile(){
        vInt++;
        System.out.println(vInt);
    }
    public void  addNormal(){
        nInt++;
        System.out.println(nInt);
    }
    public void  addLock(){
        reentrantLock.lock();
        nInt ++;
        reentrantLock.unlock();
        System.out.println(nInt);
    }

}
