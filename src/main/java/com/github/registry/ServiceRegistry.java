package com.github.registry;

import com.github.constant.RegistryConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:13 2018/6/27
 * @desc zookeeper服务注册
 */
@Slf4j
@Data
public class ServiceRegistry {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String serverAddress;

    public ServiceRegistry(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void registry(String data) {
        if(data != null) {
            ZooKeeper zk = connectServer();
            if(zk != null) {
                addRootNode(zk);
                createNode(zk, data);
            }
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(serverAddress, RegistryConstants.ZK_SESSION_TIMEOUT, event -> {
                if(event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (IOException e) {
            log.error("zookeeper registry IO error");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

    public void addRootNode(ZooKeeper zk) {
        try {
            Stat stat = zk.exists(RegistryConstants.ZK_REGISTRY_PATH, false);
            if(stat == null) {
                zk.create(RegistryConstants.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createNode(ZooKeeper zk, String data) {
        byte[] bytes = data.getBytes();
        try {
            String path = zk.create(RegistryConstants.ZK_REGISTRY_PATH, bytes,ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.info("create zookeeper node ({} => {})", path, data);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
