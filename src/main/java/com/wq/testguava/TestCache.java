package com.wq.testguava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <a href="https://my.oschina.net/u/2270476/blog/1805749"></a>
 * <a href="https://blog.csdn.net/u012859681/article/details/75220605"></a>
 * <a href="https://blog.csdn.net/abc86319253/article/details/53020432"></a>
 *
 * @author 万强
 * @date 2019/6/26 09:21
 * @desc 测试Guava Cache
 */
public class TestCache {

    /**
     * 使用CacheBuilder创建Cache
     */
    @Test
    public void testCache() {
        Cache<String, Object> cache = CacheBuilder.newBuilder()
                //设置最大缓存数量，防止内存泄露
                .maximumSize(100)
                //设置写入10s缓存过期
                .expireAfterWrite(Duration.ofSeconds(5))
                //设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                //开启缓存统计
                .recordStats()
                .build();

        //放入缓存
        cache.put("A", "a");
        //取出缓存
        @Nullable Object ifPresent = cache.getIfPresent("A");
        System.out.println(ifPresent);//a

        try {
            //休眠6s模拟缓存失效
            Thread.sleep(6000);

            //再次获取缓存
            ifPresent = cache.getIfPresent("A");
            System.out.println(ifPresent);//null
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //当缓存不存在时，通过callable加载并返回，该操作是原子性的
        try {
            Object a = cache.get("A", () -> "b");
            System.out.println(a);//b
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用CacheLoader创建LoadingCache，能够通过CacheLoader自发的加载缓存
     * 1、定时过期：
     * 当获取的缓存值不存在或已过期时，则会调用CacheLoader#load方法，进行缓存值的计算
     *
     * 缺点：
     * 当大量线程用相同的key获取缓存值时，只会有一个线程进入load方法，而其他线程则等待，直到缓存值被生成。这样也就避免了缓存穿透的危险
     * 但是每当某个缓存值过期时，老是会导致大量的请求线程被阻塞
     */
    @Test
    public void test2() {
        Random random = new Random();
        LoadingCache<String, Object> loadingCache = CacheBuilder.newBuilder()
                //设置最大缓存数量，防止内存泄露
                .maximumSize(100)
                //1、设置写入5s缓存过期，通过CacheLoader的load方法进行刷新
                .expireAfterWrite(Duration.ofSeconds(5))
                //设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                //开启缓存统计
                .recordStats()
                .build(
                        //定义缓存失效时的缓存加载器
                        new CacheLoader<String, Object>() {

                            @Override
                            public Object load(String key) throws Exception {
                                int num = random.nextInt(100);
                                System.out.println(num);
                                return num;
                            }
                        });

        loadingCache.put("A", -1);

        Object a;
        try {
            // 1、获取缓存，当缓存不存在时，会通过CacheLoader自动加载，该方法会抛出ExecutionException异常
            a = loadingCache.get("A");
            System.out.println(a);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // 2、以不安全的方式获取缓存，当缓存不存在时，会通过CacheLoader自动加载，该方法不会抛出异常
        a = loadingCache.getUnchecked("k1");//不存在k1，所以每次都会随机获取
        System.out.println(a);

        // 3、查看统计信息
        System.out.println(loadingCache.stats());//CacheStats{hitCount=1, missCount=1, loadSuccessCount=1, loadExceptionCount=0, totalLoadTime=1427100, evictionCount=0}

    }

    /**
     * 2、定时刷新：
     * 更新线程调用load方法更新该缓存，其他请求线程返回该缓存的旧值。
     * 这样对于某个key的缓存来说，只会有一个线程被阻塞，用来生成缓存值，而其他的线程都返回旧的缓存值，不会被阻塞
     *
     * 注意：
     * 这里的定时并不是真正意义上的定时。Guava cache的刷新需要依靠用户请求线程，让该线程去进行load方法的调用，
     * 所以如果一直没有用户尝试获取该缓存值，则该缓存也并不会刷新
     */
    @Test
    public void test3() {
        Random random = new Random();
        LoadingCache<String, Object> loadingCache = CacheBuilder.newBuilder()
                //设置最大缓存数量，防止内存泄露
                .maximumSize(100)
                // 2、设置缓存在写入2s后，通过CacheLoader的load方法进行刷新
                .refreshAfterWrite(2, TimeUnit.SECONDS)
                //设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                //开启缓存统计
                .recordStats()
                .build(
                        //定义缓存失效时的缓存加载器
                        new CacheLoader<String, Object>() {

                            @Override
                            public Object load(String key) throws Exception {
                                int num = random.nextInt(100);
                                System.out.println(num);
                                return num;
                            }
                        });

        loadingCache.put("A", -1);

        Object a;
        try {
            // 1、获取缓存，当缓存不存在时，会通过CacheLoader自动加载，该方法会抛出ExecutionException异常
            a = loadingCache.get("A");
            System.out.println(a);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // 2、以不安全的方式获取缓存，当缓存不存在时，会通过CacheLoader自动加载，该方法不会抛出异常
        a = loadingCache.getUnchecked("k1");//不存在k1，所以每次都会随机获取
        System.out.println(a);

        // 3、查看统计信息
        System.out.println(loadingCache.stats());//CacheStats{hitCount=1, missCount=1, loadSuccessCount=1, loadExceptionCount=0, totalLoadTime=1427100, evictionCount=0}

    }

    /**
     * 3、异步刷新
     * 如2中的使用方法，解决了同一个key的缓存过期时会让多个线程阻塞的问题，只会让用来执行刷新缓存操作的一个用户线程会被阻塞。
     * 由此可以想到另一个问题，当缓存的key很多时，高并发条件下大量线程同时获取不同key对应的缓存，此时依然会造成大量线程阻塞，
     * 并且给数据库带来很大压力。这个问题的解决办法就是将刷新缓存值的任务交给后台线程，所有的用户请求线程均返回旧的缓存值，
     * 这样就不会有用户线程被阻塞了
     */
    @Test
    public void test4() {
        Random random = new Random();
        /**
         * 新建了一个线程池，用来执行缓存刷新任务。并且重写了CacheLoader的reload方法，在该方法中建立缓存刷新的任务并提交到线程池。
         * 注意此时缓存的刷新依然需要靠用户线程来驱动，只不过和2不同之处在于该用户线程触发刷新操作之后，会立马返回旧的缓存值
         */
        ListeningExecutorService backgroundRefreshPools =
                MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(20));
        LoadingCache<String, Object> caches = CacheBuilder.newBuilder()
                .maximumSize(100)
                .refreshAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(String key) throws Exception {
                        return random.nextInt(100);
                    }

                    @Override
                    public ListenableFuture<Object> reload(String key,
                                                           Object oldValue) throws Exception {
                        return backgroundRefreshPools.submit(new Callable<Object>() {

                            @Override
                            public Object call() throws Exception {
                                return random.nextInt(100);
                            }
                        });
                    }
                });
        try {
            System.out.println(caches.get("key-zorro"));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }



}
