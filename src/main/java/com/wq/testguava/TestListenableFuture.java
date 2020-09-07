package com.wq.testguava;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * guava提供了以下几种方式添加回调
 * <p>
 * 1、ListenableFuture接口上的addLister(Runnbale, Executor)
 * 2、Futures.addCallback(ListenableFuture, FutureCallback<? super V>, Executor)
 * 3、Futures.addCallback(ListenableFuture, FutureCallback<? super V>) // 这种情况默认使用MoreExecutors.sameThreadExecutor()线程池
 *
 * @version 1.0
 * @auther 万强
 * @date 2019/9/12 14:56
 * @desc Guava并发编程之ListenableFuture
 */
public class TestListenableFuture {


    /**
     * 生产车票
     *
     * @return
     */
    public List<String> createTickets() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add("车票" + i);
        }
        return list;
    }

    /**
     * 卖车票
     */
    @Test
    public void sellTicket1() {
        List<String> list = createTickets();//获取车票

        /*获取线程执行的结果*/
        List<ListenableFuture<String>> futures = Lists.newArrayList();
        ExecutorService pool = Executors.newFixedThreadPool(10);//定义线程数

        /*定义监听线程池的服务*/
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(pool);
        for (String s : list) {
            ListenableFuture<String> future = executorService.submit(new Task(s));
            /*给future添加监听器，用于注册回调方法*/
            future.addListener(
                    /*第一个参数是Runnable，会在任务task执行完后执行*/
                    () -> System.out.println("任务执行完毕！"),
                    /*第二个参数是Executor，用于执行第一个参数的Runnable*/
                    Runnable::run
            );
            futures.add(future);
        }

        final ListenableFuture<List<String>> resultsFuture = Futures.successfulAsList(futures);
        try {

            //获取所有task的执行结果
            List<String> strings = resultsFuture.get();
            System.out.println("所有任务执行完毕，results：" + strings);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("操作完毕");
            pool.shutdown();
        }
    }

    @Test
    public void sellTicket2() {
        List<String> list = createTickets();//获取车票

        /*获取线程执行的结果*/
        List<ListenableFuture<String>> futures = Lists.newArrayList();
        ExecutorService pool = Executors.newFixedThreadPool(10);//定义线程数

        /*定义监听线程池的服务*/
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(pool);
        for (String s : list) {
            ListenableFuture<String> future = executorService.submit(new Task(s));
            /*给future添加监听器，用于注册回调方法*/
            future.addListener(
                    /*第一个参数是Runnable，会在任务task执行完后执行*/
                    () -> System.out.println("任务执行完毕！"),
                    /*第二个参数是Executor，用于执行第一个参数的Runnable*/
                    Runnable::run
            );
            futures.add(future);
        }

        try {
            //获取所有task的执行结果
            futures.forEach(future -> {
                try {
                    System.out.println("任务执行结果--->>>"+future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("所有任务执行完毕");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("操作完毕");
            pool.shutdown();
        }
    }


    /**
     * 内部类，用于处理售票
     */
    class Task implements Callable<String> {
        private String ticket;

        /**
         * 构造方法，用于参数传递
         *
         * @param ticket
         */
        public Task(String ticket) {
            this.ticket = ticket;
        }

        @Override
        public String call() throws Exception {
            System.out.println("已卖" + ticket);//执行卖票过程
            return ticket;
        }
    }

}
 
