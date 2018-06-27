package com.github.client.proxy;

import com.github.client.RemoteFuture;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:06 2018/6/27
 * @desc
 */
@Slf4j
public class ObjectProxy<T> implements AsyncObjectProxy,InvocationHandler {

    private Class<T> clazz;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public RemoteFuture call(String name, Object... args) {
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
