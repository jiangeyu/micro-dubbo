package com.github.constant;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午11:08 2018/6/28
 * @desc 常量类
 */
public interface RegistryConstants {

    int ZK_SESSION_TIMEOUT = 50000;

    String ZK_REGISTRY_PATH = "/registry";

    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
