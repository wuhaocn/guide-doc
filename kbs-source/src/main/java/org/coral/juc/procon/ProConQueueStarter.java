package org.coral.juc.procon;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ProConQueueStarter {
    public static AtomicLong atomicLong = new AtomicLong(0);
    public static  BlockingQueue<String> cur = new ArrayBlockingQueue<>(1);
    public static void main(String[] args) {

        Thread tc1 = new Thread(new Consumer(cur, 100));
//        Thread tc2 = new Thread(new Consumer(cur, 100));
        Thread tc3 = new Thread(new Consumer(cur, 100));
        tc1.start();
//        tc2.start();
//        tc3.start();

        Thread tp1 = new Thread(new Producer(cur, 100));
//        Thread tp2 = new Thread(new Producer(cur, 100));
//        Thread tp3 = new Thread(new Producer(cur, 100));
        tp1.start();
//        tp2.start();
//        tp3.start();

    }
    static class Producer implements Runnable{
        private Queue<String> curQueue;
        private int count;

        public Producer(Queue<String> curQueue, int count) {
            this.curQueue = curQueue;
            this.count = count;

        }

        @Override
        public void run() {
            while (true){
                synchronized (curQueue){
                    StringBuilder sb = new StringBuilder();
                    sb.append("producer-");
                    sb.append(Thread.currentThread().getName());
                    sb.append(":");
                    sb.append(atomicLong.incrementAndGet());
                    boolean put = curQueue.offer(sb.toString());
                    try {
                        if (!put){
                            atomicLong.decrementAndGet();
                            curQueue.wait();
                            System.out.println(sb.toString() + "wait");
                        } else {
                            System.out.println(sb.toString() + "ok");
                        }
                        curQueue.notifyAll();

                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }


            }
        }
    }
    static class Consumer implements Runnable{
        private Queue<String> curQueue;
        private int count;


        public Consumer(Queue<String> curQueue, int count) {
            this.curQueue = curQueue;
            this.count = count;
        }

        @Override
        public void run() {
            while (true){
                synchronized (curQueue){
                    StringBuilder sb = new StringBuilder();
                    sb.append("consumer-");
                    sb.append(Thread.currentThread().getName());
                    sb.append(":");
                    String consumer = curQueue.poll();
                    if (consumer == null){
                        try {
                            curQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println(sb.toString() + "wait");
                    } else {
                        sb.append("cp: ");
                        sb.append(consumer);
                        System.out.println(sb.toString());
                    }
                    curQueue.notifyAll();


                }

            }
        }
    }

}
