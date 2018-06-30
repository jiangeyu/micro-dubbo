package com.github.client;

import com.github.code.RpcDecoder;
import com.github.code.RpcEncoder;
import com.github.protocol.Request;
import com.github.protocol.Response;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午9:54 2018/6/30
 * @desc
 */
public class RemoteClientInitializerHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RpcEncoder(Request.class));
        pipeline.addLast(new RpcDecoder(Response.class));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535,0,4,0,0));
        pipeline.addLast(new RemoteClientHandler());
    }
}
