package com.github.zookeeper;

/**
 * Created by tumingjian on 2017/3/11.
 */
public interface ZookeeperConfig {
    int getConnectionTimeout();

    String getConnectString();

    int getSessionTimeout();

    String getNamespace();
}
