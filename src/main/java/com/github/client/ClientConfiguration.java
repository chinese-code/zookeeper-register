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
    private String servicePath;
    public ClientConfiguration(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String servicePath) {
        super(connectString, connectionTimeout, sessionTimeout, namespace);
        this.servicePath = servicePath;
    }

    public ClientConfiguration(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String servicePath, ServerVerifyHandler serverVerifyHandler) {
        this(connectString,connectionTimeout,sessionTimeout,namespace, servicePath);
    }

    public ClientConfiguration(ZookeeperConfig zookeeperConfig, String servicePath, ServerVerifyHandler serverVerifyHandler) {
        super(zookeeperConfig.getConnectString(),zookeeperConfig.getConnectionTimeout(),zookeeperConfig.getSessionTimeout(),zookeeperConfig.getNamespace());
        this.servicePath = servicePath;
    }
    public ClientConfiguration(ZookeeperConfig zookeeperConfig, String servicePath) {
        this(zookeeperConfig, servicePath,null);
    }
    public ClientConfiguration() {

    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }
}
