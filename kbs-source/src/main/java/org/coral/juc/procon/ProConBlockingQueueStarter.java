package org.coral.juc.procon;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProConBlockingQueueStarter {
    public static void main(String[] args) {
        BlockingQueue<String> cur = new ArrayBlockingQueue<>(30);
        Thread tc1 = new Thread(new Consumer(cur, 10));
        Thread tc2 = new Thread(new Consumer(cur, 10));
        Thread tc3 = new Thread(new Consumer(cur, 10));
        tc1.start();
        tc2.start();
        tc3.start();

        Thread tp1 = new Thread(new Producer(cur, 10));
        Thread tp2 = new Thread(new Producer(cur, 10));
        Thread tp3 = new Thread(new Producer(cur, 10));
        tp1.start();
        tp2.start();
        tp3.start();

    }
    static class Producer implements Runnable{
        private BlockingQueue<String> curQueue;
        private int count;

        public Producer(BlockingQueue<String> curQueue, int count) {
            this.curQueue = curQueue;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++){
                synchronized (curQueue){
                    StringBuilder sb = new StringBuilder();
                    sb.append("producer-");
                    sb.append(Thread.currentThread().getName());
                    sb.append(":");
                    sb.append(i);
                    boolean put = curQueue.offer(sb.toString());
                    try {
                        if (!put){
                            this.wait();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }


            }
        }
    }
    static class Consumer implements Runnable{
        private BlockingQueue<String> curQueue;
        private int count;

        public Consumer(BlockingQueue<String> curQueue, int count) {
            this.curQueue = curQueue;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++){
                synchronized (curQueue){
                    StringBuilder sb = new StringBuilder();
                    sb.append("consumer-");
                    sb.append(Thread.currentThread().getName());
                    sb.append(":");
                    sb.append(i);
                    String consumer = curQueue.poll();
                    if (consumer == null){
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sb.append("cp: ");
                    sb.append(consumer);
                    System.out.println(sb.toString());
                }

            }
        }
    }

}
