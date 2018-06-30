package com.github.client;

import com.github.client.proxy.AsyncObjectProxy;
import com.github.client.proxy.ObjectProxy;
import com.github.registry.ConnectionManager;
import com.github.registry.ServiceDiscovery;
import lombok.Data;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:56 2018/6/27
 * @desc
 */
@Data
public class RemoteClient {

    private String serverAddress;

    private ServiceDiscovery serviceDiscovery;

    public RemoteClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024));

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<>(interfaceClass)
        );
    }

    public static <T> AsyncObjectProxy createAsyns(Class<T> clazz) {
       return new ObjectProxy<>(clazz);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectionManager.getInstance().stop();
    }
}
