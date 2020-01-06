package org.coral.juc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ManualDiceRollsOne {
    //投掷两次骰子的次数
    private static final int N = 100000000;
    //一次的占比
    private final double fraction;
    //每次投2次骰子的点数之和与概率的映射
    private final Map<Integer,Double> results;
    //计算机线程数
    private final int numbersOfThreads;
    //线程池
    private final ExecutorService executor;
    //每个线程的工作次数
    private final int workPerThread;


    public ManualDiceRollsOne() {
        fraction = 1.0 / N;
        results = new ConcurrentHashMap<>();
        numbersOfThreads = Runtime.getRuntime().availableProcessors() * 2;
        executor = Executors.newFixedThreadPool(numbersOfThreads);
        workPerThread = N / numbersOfThreads;
    }

    public void simulateDiceRoles() {
        //计算所有投掷2次骰子的结果概率
        List<Future<?>> futures = submitJobs();
        //等待结果，拿取结果
        awaitCompletion(futures);
        //打印结果
        printResults();
    }

    private void printResults() {
        //等同于results.entrySet().forEach(entry -> System.out.println(entry));
        results.entrySet().forEach(System.out::println);
    }

    private List<Future<?>> submitJobs() {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0;i < numbersOfThreads;i++) {
            //我把我的所有计算任务全部交给Future集合，彼此间不影响
            futures.add(executor.submit(makeJob()));
        }

        return futures;
    }

    private Runnable makeJob() {
        return () -> {
            //ThreadLocalRandom对应于不同线程都有一个线程的随机种子值
            //在多线程下当使用ThreadLocalRandom来生成随机数
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0;i < workPerThread;i++) {
                int entry = twoDiceThrows(random);
                //获取每次投掷2个骰子的点数之和，增加每次的概率(亿分之一)，存入
                //线程安全集合ConcurrentHashMap中
                accumuLateResult(entry);
            }
        };
    }

    private void accumuLateResult(int entry) {
        //Map的compute方法第二参数为BiFunction的函数式接口，给定两种不同的参数对象，返回另一个结果对象，这三种对象
        //可以相同，可以不同
        //如果results的entry键的值为null(该键不存在)，则把该值设为fraction(单次概率亿分之一)
        //否则将该键的值设为原值加上fraction(单次概率亿分之一)
        results.compute(entry,(key,previous) -> previous == null ? fraction : previous + fraction);
    }

    private int twoDiceThrows(ThreadLocalRandom random) {
        int firstThrow = random.nextInt(1,7);
        int secondThrow = random.nextInt(1,7);
        return firstThrow + secondThrow;
    }

    private void awaitCompletion(List<Future<?>> futures) {
        //等待所有的计算任务完成后，拿取计算结果，关闭线程池
        futures.forEach(future -> {
            try {
                future.get();
            }catch (Exception e) {
                e.printStackTrace();
            }
        });
        executor.shutdown();
    }

    public static void main(String[] args) {
        ManualDiceRollsOne rolls = new ManualDiceRollsOne();
        long start = System.currentTimeMillis();
        rolls.simulateDiceRoles();
        System.out.println(System.currentTimeMillis() - start);
    }
}