package org.coral.juc.forkjoin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ForkJoinPoolDemo {
    static AtomicLong atomicLong = new AtomicLong(0);
    class SendMsgTask extends RecursiveAction {

        private final int THRESHOLD = 10;

        private int start;
        private int end;
        private List<Integer> list;

        public SendMsgTask(int start, int end, List<Integer> list) {
            this.start = start;
            this.end = end;
            this.list = list;
        }

        @Override
        protected void compute() {
            //System.out.println("exec start " + "start:" + start +"end:" + end + "---" + (end -start));
            if ((end - start) <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    //System.out.println(Thread.currentThread().getName() + ": " + list.get(i));
                    atomicLong.getAndAdd(list.get(i));
                }
                //System.out.println("exec ok " + "start:" + start +"end:" + end);
            }else {
                int middle = (start + end) / 2;
                invokeAll(new SendMsgTask(start, middle, list), new SendMsgTask(middle, end, list));
            }

        }

    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 6000; i++) {
            list.add(i+1);
        }
        long time = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool();
        pool.submit(new ForkJoinPoolDemo().new SendMsgTask(0, list.size(), list));
       // pool.awaitTermination(100, TimeUnit.SECONDS);
        pool.awaitQuiescence(100, TimeUnit.SECONDS);
        pool.shutdown();
        System.out.println("count:" + atomicLong.get() + " time:" + (System.currentTimeMillis() - time));
    }

}