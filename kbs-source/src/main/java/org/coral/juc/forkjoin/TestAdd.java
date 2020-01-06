package org.coral.juc.forkjoin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TestAdd {
    static AtomicLong atomicLong = new AtomicLong(0);


    public static void main(String[] args) throws InterruptedException {
//        List<Integer> list = new ArrayList<>();
//        for (int i = 0; i < 6000; i++) {
//            list.add(i+1);
//        }
//        long time = System.currentTimeMillis();
//        for (int j = 0; j< list.size() ; j++) {
//            atomicLong.getAndAdd(list.get(j));
//
//        }
        System.out.println("1231" + new char[]{1, 2});
//        List features = Arrays.asList("1dxxx", "2", "3", "4","5");
//        features.forEach(n -> {
//            try {
//                FileInputStream inputStream = new FileInputStream("");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            long xx = Long.parseLong((String) n);
//            System.out.println(xx);
//        });
//        System.out.println("count:" + atomicLong.get() + " time:" + (System.currentTimeMillis() - time));
    }

}