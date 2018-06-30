package com.github.client;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午1:58 2018/6/27
 * @desc 异步回调结果状态
 */
public interface AsyncRemoteCallback {

    /**
     *  异步回调成功
     *
     * @param result
     */
    void success(Object result);

    /**
     * 异步回调失败
     *
     * @param e
     */
    void fail(Exception e);
}
