package com.github.server;

import com.github.annotation.RemoteService;
import com.github.code.RpcDecoder;
import com.github.code.RpcEncoder;
import com.github.protocol.Request;
import com.github.protocol.Response;
import com.github.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:56 2018/6/27
 * @desc
 */
@Data
public class RemoteServer implements ApplicationContextAware, InitializingBean {

    private String serverAddress;

    private ServiceRegistry serviceRegistry;

    private Map<String, Object> beanMap = new HashMap<>();

    private static ThreadPoolExecutor threadPoolExecutor;

    private EventLoopGroup bossGroup = null;

    private EventLoopGroup workGroup = null;

    public RemoteServer() {

    }

    public RemoteServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RemoteService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            serviceBeanMap.values().stream().forEach(serviceBean -> {
                String interfaceName = serviceBeanMap.getClass().getAnnotation(RemoteService.class).value().getName();
                beanMap.put(interfaceName, serviceBean);
            });
        }
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
    }

    public static void submit(Runnable task) {
        if (threadPoolExecutor != null) {
            synchronized (RemoteServer.class) {
                threadPoolExecutor.submit(task);
            }
        }
    }

    public RemoteServer addService(String interfaceName, Object serviceBean) {
        if (!beanMap.containsKey(serviceBean)) {
            beanMap.put(interfaceName, serviceBean);
        }
        return this;
    }

    public void start() throws InterruptedException {
        if (bossGroup == null && workGroup == null) {
            bossGroup = new NioEventLoopGroup();
            workGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0))
                                    .addLast(new RpcEncoder(Request.class))
                                    .addLast(new RpcDecoder(Response.class))
                                    .addLast(new RemoteServerHandler(beanMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] address = serverAddress.split(":");
            ChannelFuture future = bootstrap.bind(address[0], Integer.parseInt(address[1]));
            if (serviceRegistry != null) {
                serviceRegistry.registry(serverAddress);
            }
            future.channel().closeFuture().sync();
        }
    }
}
