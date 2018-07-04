package com.github.registry;

import com.github.client.RemoteClientHandler;
import com.github.client.RemoteClientInitializerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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

    private Condition connected = lock.newCondition();

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
        if (allServerAddress != null && allServerAddress.size() > 0) {
            Set<InetSocketAddress> nodeSet = allServerAddress.stream()
                    .map(serverAddress -> {
                        String[] address = serverAddress.split(":");
                        InetSocketAddress inetSocketAddress;
                        if (address.length == 2) {
                            int port = Integer.parseInt(address[1]);
                            inetSocketAddress = new InetSocketAddress(address[0], port);
                        } else {
                            inetSocketAddress = null;
                        }
                        return inetSocketAddress;
                    })
                    .collect(Collectors.toSet());

            nodeSet.stream().forEach(node -> connectServerNode(node));

            connectHandlers.stream().forEach(handler -> {
                SocketAddress address = handler.getRemotePeer();
                if (!nodeSet.contains(address)) {
                    RemoteClientHandler clientHandler = connectServerNodes.get(address);
                    if (clientHandler != null) {
                        clientHandler.close();
                    }
                }
                connectServerNodes.remove(address);
                connectHandlers.remove(handler);
            });

        } else {
            connectHandlers.stream().forEach(handler -> {
                SocketAddress address = handler.getRemotePeer();
                RemoteClientHandler clientHandler = connectServerNodes.get(address);
                clientHandler.close();
                connectHandlers.remove(handler);
            });
            connectHandlers.clear();
        }
    }

    public void reconnet(RemoteClientHandler remoteClientHandler, SocketAddress remotePeer) {
        if (remoteClientHandler != null) {
            connectHandlers.remove(remoteClientHandler);
            connectServerNodes.remove(remoteClientHandler.getRemotePeer());
        }
        connectServerNode((InetSocketAddress) remotePeer);
    }

    public void connectServerNode(InetSocketAddress address) {
        threadPoolExecutor.submit(() -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RemoteClientInitializerHandler());

            ChannelFuture channelFuture = bootstrap.connect(address);

            channelFuture.addListener((ChannelFutureListener) future -> {
                if(future.isSuccess()) {
                    RemoteClientHandler handler = future.channel().pipeline().get(RemoteClientHandler.class);
                    addHandler(handler);
                }
            });
        });
    }

    private void addHandler(RemoteClientHandler handler) {
        connectHandlers.add(handler);
        InetSocketAddress address = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectServerNodes.put(address, handler);
        signalAvailableHandler();
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signal();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return connected.await(this.connectTimeoutMills, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public RemoteClientHandler chooseHandler() {
        int size = connectHandlers.size();
        while (isRunning && size <= 0) {
            try {
                boolean available = waitingForHandler();
                if (available) {
                    size = connectHandlers.size();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return connectHandlers.get(index);
    }

    public void stop() {
        isRunning = false;
        connectHandlers.stream().forEach( handler -> handler.close());
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
