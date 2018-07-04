package com.github.code;

import com.github.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:26 2018/6/30
 * @desc
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;

    public RpcDecoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int length = in.readInt();
        if (length < 0) {
            ctx.close();
        }
        if(in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[length];
        in.readBytes(data);

        Object object = SerializationUtil.deserialize(data, clazz);
        out.add(object);
    }
}
