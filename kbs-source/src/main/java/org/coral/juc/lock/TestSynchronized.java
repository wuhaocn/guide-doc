package org.coral.juc.lock;

public class TestSynchronized {
    static int i = 0;
    public static void main(String[] args) {
        printHello();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int k = 0 ; k < 1000; k ++) {
                   // printHello();
                }
            }
        });
        thread.start();
    }
    public synchronized static void printHello(){

        synchronized (TestSynchronized.class){
            i++;
        }
        System.out.println("hello world");

    }
}
