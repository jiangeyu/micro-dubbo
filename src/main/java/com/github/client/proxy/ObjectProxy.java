package com.github.client.proxy;

import com.github.client.RemoteClientHandler;
import com.github.client.RemoteFuture;
import com.github.protocol.Request;
import com.github.registry.ConnectionManager;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.stream.IntStream;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:06 2018/6/27
 * @desc
 */
@Slf4j
public class ObjectProxy<T> implements AsyncObjectProxy, InvocationHandler {

    private Class<T> clazz;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public RemoteFuture call(String methodName, Object... args) {
        RemoteClientHandler handler = ConnectionManager.getInstance().chooseHandler();
        Request request = createRequest(this.clazz.getName(), methodName, args);
        RemoteFuture future = handler.sendRequest(request);
        return future;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class == method.getDeclaringClass()) {
            String methodName = method.getName();
            if("equals".equals(methodName)) {
                return proxy == args[0];
            } else if("toString".equals(methodName)) {
                return System.identityHashCode(proxy);
            } else if("toString".equals(methodName)) {
               return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with invocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        Request request = Request.builder()
                .requestId(Request.newId())
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .parameterTypes(method.getParameterTypes())
                .build();

        RemoteClientHandler clientHandler = ConnectionManager.getInstance().chooseHandler();
        RemoteFuture future = clientHandler.sendRequest(request);
        return future.get();
    }

    private Request createRequest(String className, String methodName, Object[] args) {
        Request request = Request
                .builder()
                .requestId(Request.newId())
                .className(className)
                .methodName(methodName)
                .parameters(args)
                .build();
        Class[] parameterTypes = new Class[args.length];
        IntStream.range(0, args.length).forEach(i -> parameterTypes[i] = getParameterType(args[i]));
        request.setParameterTypes(parameterTypes);
        log.info("parameter type, {}", parameterTypes.toString());

        return request;
    }

    private Class<?> getParameterType(Object object) {
        Class<?> classType = object.getClass();
        String typeName = classType.getName();
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
            default:
                log.error("unrecognized type");
        }
        return classType;
    }
}
