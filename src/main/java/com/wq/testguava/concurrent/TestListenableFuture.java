package com.wq.testguava.concurrent;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * @author 万强
 * @version 1.0
 * @date 2020/8/14 14:14
 * @desc ListenableFuture顾名思义就是可以监听的Future，它是对java原生Future的扩展增强。
 * 我们知道Future表示一个异步计算任务，当任务完成时可以得到计算结果。如果我们希望一旦计算完成就拿到结果展示给用户或者做另外的计算，
 * 就必须使用另一个线程不断的查询计算状态。这样做，代码复杂，而且效率低下。使用ListenableFuture Guava帮我们检测Future是否完成了，
 * 如果完成就自动调用回调函数，这样可以减少并发程序的复杂度
 */
@Slf4j
public class TestListenableFuture {

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(false).setNameFormat("test-pool-%d").build();

    private static final ExecutorService delegate = new ThreadPoolExecutor(20,
            100, 60, TimeUnit.SECONDS, new SynchronousQueue<>(false), threadFactory);

    private static final ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(delegate);

    /**
     * 并发执行任务，但不阻塞main线程
     */
    @Test
    public void test1() {

        ArrayList<ListenableFuture<Integer>> list = Lists.newArrayList();
        /* TODO
         * 注意：由于使用ListenableFuture纯异步，所以如果通过循环创建任务，
         * 那么务必注意线程池的maximumPoolSize需要>=循环次数，否则会因为线程数量达到上限无法继续提交任务而报错！！！
         */
        IntStream.range(0, 100).forEach(i -> {
            log.info("开始执行第{}个task", i);

            ListenableFuture<Integer> listenableFuture = listeningExecutorService.submit(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                    //模拟其中1个任务执行报错
//                    if (i == 8) {
//                        System.out.println(1 / 0);
//                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i;
            });

            list.add(listenableFuture);

            /*
             * listenableFuture执行完后的回调，有2种添加方式：
             * 1、直接为listenableFuture添加监听器，执行完即回调
             * 2、通过Futures的静态方法addCallback给ListenableFuture添加回调函数
             *
             * 推荐使用第2种方式，因为可以直接拿到返回值和异常
             */

            /*1、直接为listenableFuture添加监听器，执行完即回调*/
            listenableFuture.addListener(
                    //第1个参数是个Runnable，当任务执行完成后会执行此回调
                    () -> {
                        try {
                            log.info("listenableFuture异步处理完成，result = {}", listenableFuture.get());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    },
                    //第2个参数是Executor，用于执行第1个参数的Runnable
                    Runnable::run);

            /*2、通过Futures的静态方法addCallback给ListenableFuture添加回调函数*/
            Futures.addCallback(
                    //第1个参数为要监听的listenableFuture
                    listenableFuture,
                    //第2个参数为listenableFuture执行完后的回调
                    new FutureCallback<Integer>() {
                        @Override
                        public void onSuccess(@Nullable Integer result) {
                            log.info("listenableFuture异步处理成功，result = {}", result);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            log.error("listenableFuture异步处理失败", t);

                        }
                    },
                    //第3个参数是Executor，用于执行第1个参数的listenableFuture
                    Runnable::run);
        });

        ListenableFuture[] listenableFutures = list.toArray(new ListenableFuture[0]);

        /*
         * 当上面10个任务全部执行成功后，执行新的listenableFuture
         */
//        Callable<String> callable = () -> "所有任务执行完毕！";
//        ListenableFuture<String> future = Futures.whenAllSucceed(listenableFutures)
//                .call(callable, listeningExecutorService);

        /*
         *  TODO
         * 当上面提交的所有任务全部执行成功后，返回新的ListenableFuture，此future的返回值为所有任务的
         * 返回值的集合，且顺序与提交任务的顺序一致！（但是任务执行顺序不一定是一致的）
         */
        ListenableFuture future = Futures.successfulAsList(listenableFutures);

        /*
         * 下面这边方式也是异步执行，不会阻塞main线程，因为会出现main线程结束了，上面的异步任务还没执行完的情况
         */
        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(@Nullable Object result) {
                log.info("任务执行成功， result = {}", result);
            }

            @Override
            public void onFailure(Throwable t) {
                log.info("任务执行失败", t);

            }
        }, listeningExecutorService);

        log.info("main线程执行完成！");
//        while (true) {
//        }
    }

    /**
     * 并发执行任务，阻塞main线程（即并发任务执行完后，才会继续执行main线程）
     */
    @Test
    public void test2() {

        ArrayList<ListenableFuture<Integer>> list = Lists.newArrayList();
        /* TODO
         * 注意：由于使用ListenableFuture纯异步，所以如果通过循环创建任务，
         * 那么务必注意线程池的maximumPoolSize需要>=循环次数，否则会因为线程数量达到上限无法继续提交任务而报错！！！
         */
        IntStream.range(0, 100).forEach(i -> {
            log.info("开始执行第{}个task", i);

            ListenableFuture<Integer> listenableFuture = listeningExecutorService.submit(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                    //模拟其中1个任务执行报错
//                    if (i == 8) {
//                        System.out.println(1 / 0);
//                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i;
            });

            list.add(listenableFuture);

            /*
             * listenableFuture执行完后的回调，有2种添加方式：
             * 1、直接为listenableFuture添加监听器，执行完即回调
             * 2、通过Futures的静态方法addCallback给ListenableFuture添加回调函数
             *
             * 推荐使用第2种方式，因为可以直接拿到返回值和异常
             */

            /*1、直接为listenableFuture添加监听器，执行完即回调*/
            listenableFuture.addListener(
                    //第1个参数是个Runnable，当任务执行完成后会执行此回调
                    () -> {
                        try {
                            log.info("listenableFuture异步处理完成，result = {}", listenableFuture.get());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    },
                    //第2个参数是Executor，用于执行第1个参数的Runnable
                    Runnable::run);

            /*2、通过Futures的静态方法addCallback给ListenableFuture添加回调函数*/
            Futures.addCallback(
                    //第1个参数为要监听的listenableFuture
                    listenableFuture,
                    //第2个参数为listenableFuture执行完后的回调
                    new FutureCallback<Integer>() {
                        @Override
                        public void onSuccess(@Nullable Integer result) {
                            log.info("listenableFuture异步处理成功，result = {}", result);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            log.error("listenableFuture异步处理失败", t);

                        }
                    },
                    //第3个参数是Executor，用于执行第1个参数的listenableFuture
                    Runnable::run);
        });

        ListenableFuture[] listenableFutures = list.toArray(new ListenableFuture[0]);

        /*
         * 当上面10个任务全部执行成功后，执行新的listenableFuture
         */
//        Callable<String> callable = () -> "所有任务执行完毕！";
//        ListenableFuture<String> future = Futures.whenAllSucceed(listenableFutures)
//                .call(callable, listeningExecutorService);

        /*
         *  TODO
         * 当上面提交的所有任务全部执行成功后，返回新的ListenableFuture，此future的返回值为所有任务的
         * 返回值的集合，且顺序与提交任务的顺序一致！（但是任务执行顺序不一定是一致的）
         */
        ListenableFuture future = Futures.successfulAsList(listenableFutures);

        /*
         * 并发执行上面提交的任务，但会阻塞main线程，执行完后才继续执行main线程
         */
        try {
            System.out.println("所有任务执行完毕，results：" + future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        log.info("main线程执行完成！");
//        while (true) {
//        }
    }


}
 
