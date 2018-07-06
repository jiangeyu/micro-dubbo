package com.github.protocol;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:41 2018/6/27
 * @desc 请求封装
 */
@Builder
@Data
public class Request {

    /**
     * 全局唯一id标志一次请求
     */
    private Long requestId;

    private String className;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;

    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    public static Long newId() {
        return INVOKE_ID.getAndIncrement();
    }


}
