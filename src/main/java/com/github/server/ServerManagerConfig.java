package com.github.server;

import com.github.service.ServiceConfiguration;
import com.github.zookeeper.ZookeeperConfig;

/**
 * Created by tumingjian on 2017/3/12.
 */
public interface ServerManagerConfig extends ZookeeperConfig {
    String getServiceName();

    String getHost();

    String getPort();

    ServiceConfiguration getServiceConfiguration();
}
