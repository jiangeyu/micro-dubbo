package com.github.client;

import com.github.protocol.Request;
import com.github.protocol.Response;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:02 2018/6/29
 * @desc
 */
@Slf4j
public class RemoteClientHandler extends SimpleChannelInboundHandler<Response> {

    private ConcurrentHashMap<Long, RemoteFuture> futureMap = new ConcurrentHashMap<>();

    @Getter
    private volatile Channel channel;
    @Getter
    private SocketAddress remotePeer;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        long requestId = response.getRequestId();
        RemoteFuture future = futureMap.get(requestId);
        if (future != null) {
            futureMap.remove(requestId);
            future.done(response);
        }
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RemoteFuture sendRequest(Request request) {
        final CountDownLatch latch = new CountDownLatch(1);
        RemoteFuture future = new RemoteFuture(request);
        futureMap.put(request.getRequestId(), future);
        channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> latch.countDown());
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return future;
    }

}
