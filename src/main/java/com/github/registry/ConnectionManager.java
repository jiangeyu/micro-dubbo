package com.github.registry;

import com.github.client.RemoteClientHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:36 2018/6/29
 * @desc 服务注册管理
 */
public class ConnectionManager {

    private volatile static ConnectionManager connectionManager;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 4,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024));
    private CopyOnWriteArrayList<RemoteClientHandler> connectHandlers = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress, RemoteClientHandler> connectServerNodes = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private long connectTimeoutMills = 6000;
    private AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean isRunning = true;

    public static ConnectionManager getInstance() {
        if (connectionManager == null) {
            synchronized (ConnectionManager.class) {
                connectionManager = new ConnectionManager();
            }
        }
        return connectionManager;
    }

    public void updateConnectServer(List<String> allServerAddress) {

    }
}
