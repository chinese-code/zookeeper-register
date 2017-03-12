package com.github.client;

import com.github.zookeeper.ZookeeperConfig;
import com.github.zookeeper.ZookeeperConfiguration;

/**
 * Created by tumingjian on 2017/3/12.
 */
public class ClientConfiguration extends ZookeeperConfiguration {
    /**
     * 服务名
     */
    private String serviceName;
    public ClientConfiguration(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String serviceName) {
        super(connectString, connectionTimeout, sessionTimeout, namespace);
        this.serviceName = serviceName;
    }

    public ClientConfiguration(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String serviceName, ServerVerifyHandler serverVerifyHandler) {
        this(connectString,connectionTimeout,sessionTimeout,namespace,serviceName);
    }

    public ClientConfiguration(ZookeeperConfig zookeeperConfig, String serviceName, ServerVerifyHandler serverVerifyHandler) {
        super(zookeeperConfig.getConnectString(),zookeeperConfig.getConnectionTimeout(),zookeeperConfig.getSessionTimeout(),zookeeperConfig.getNamespace());
        this.serviceName = serviceName;
    }
    public ClientConfiguration(ZookeeperConfig zookeeperConfig, String serviceName) {
        this(zookeeperConfig,serviceName,null);
    }
    public ClientConfiguration() {

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
