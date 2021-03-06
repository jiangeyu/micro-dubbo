package com.github.server;

import com.github.protocol.Request;
import com.github.protocol.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:00 2018/6/27
 * @desc
 */
@Slf4j
public class RemoteServerHandler extends SimpleChannelInboundHandler<Request> {

    /**
     * 实现的服务类map
     *
     */
    private final Map<String, Object> serviceBeanMap;

    public RemoteServerHandler(Map<String, Object> handlerMap) {
        this.serviceBeanMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        RemoteServer.submit(() -> {
            Response response =  Response.builder().build();
            response.setRequestId(request.getRequestId());
            try {
                Object result = handleCglib(request);
                response.setResult(result);
            } catch (Throwable throwable) {
                response.setError(throwable.toString());
                throwable.printStackTrace();
                log.error("handler request error, requestId = {}", request.getRequestId());
            }
            ctx.writeAndFlush(response).addListener(future -> {
                log.info("send response for request , requestId = {}", request.getRequestId());
            });
        });
    }


    /**
     * 创建注册的服务类，使用cglib方式
     *
     * @param request
     * @return
     * @throws Throwable
     */
    private Object handleCglib(Request request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = serviceBeanMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        Arrays.stream(parameterTypes).forEach(parameterType -> System.out.println("parameterType ---- name" + parameterType.getName()));
        Arrays.stream(parameters).forEach(parameterType -> System.out.println("parameterType ---- name" + parameterType.toString()));
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);

    }

    /**
     * 创建注册的服务类，使用jdk动态代理方式
     *
     * @param request
     * @return
     * @throws Throwable
     */
    private Object handleJdk(Request request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = serviceBeanMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        Arrays.stream(parameterTypes).forEach(parameterType -> System.out.println("parameterType ---- name" + parameterType.getName()));
        Arrays.stream(parameters).forEach(parameterType -> System.out.println("parameterType ---- name" + parameterType.toString()));

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return  method.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server caught exception");
        ctx.close();
    }
}
