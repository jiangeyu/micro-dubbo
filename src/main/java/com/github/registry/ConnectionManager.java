package com.github.registry;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:36 2018/6/29
 * @desc 服务注册管理
 */
public class ConnectionManager {

    private volatile  static ConnectionManager connectionManager;

    public static ConnectionManager getInstance() {
        if(connectionManager == null) {
            synchronized (ConnectionManager.class) {
                connectionManager = new ConnectionManager();
            }
        }
        return connectionManager;
    }

    public void updateConnectServer(List<String> allServerAddress) {

    }
}
