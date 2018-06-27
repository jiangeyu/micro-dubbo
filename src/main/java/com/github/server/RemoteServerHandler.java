package com.github.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:00 2018/6/27
 * @desc
 */
public class RemoteServerHandler extends SimpleChannelInboundHandler<RemoteServer> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemoteServer msg) throws Exception {

    }
}
