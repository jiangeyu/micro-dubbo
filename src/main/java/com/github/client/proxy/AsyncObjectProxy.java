package com.github.client.proxy;

import com.github.client.RemoteFuture;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:54 2018/6/27
 * @desc
 */
public interface AsyncObjectProxy {

    public RemoteFuture call(String name, Object... args);

}
