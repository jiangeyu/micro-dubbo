package com.github.registry;

import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:12 2018/6/27
 * @desc 服务发现
 */
public class ServiceDiscovery {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String registryAddress;

    private ZooKeeper zk;


}
