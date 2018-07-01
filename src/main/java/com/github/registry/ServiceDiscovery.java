package com.github.registry;

import com.github.constant.RegistryConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:12 2018/6/27
 * @desc 服务发现
 */
@Slf4j
public class ServiceDiscovery {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String registryAddress;

    private ZooKeeper zk;

    private volatile List<String> dataList = new ArrayList<>();


    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    public ZooKeeper connectServer() {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(registryAddress, RegistryConstants.ZK_SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (IOException e) {
            log.error("zookeeper discovery IO error");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

    public void watchNode(ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(RegistryConstants.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = nodeList.stream().map(data -> {
                byte[] bytes = new byte[0];
                try {
                    bytes = zk.getData(RegistryConstants.ZK_REGISTRY_PATH + "/" + data, false, null);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new String(bytes);
            }).collect(Collectors.toList());
            this.dataList = dataList;
            updateConnectServer();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updateConnectServer() {
        ConnectionManager.getInstance().updateConnectServer(this.dataList);
    }

    public void stop() {
        if(zk != null) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
