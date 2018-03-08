package com.zookeeper.service.client;

import com.zookeeper.service.config.ZookeeperConfig;
import com.zookeeper.service.config.ZookeeperConfiguration;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ClientConfig extends ZookeeperConfiguration {
    /**
     * 服务名
     */
    private String servicePathList;
    public ClientConfig(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String servicePathList) {
        super(connectString, connectionTimeout, sessionTimeout, namespace);
        this.servicePathList = servicePathList;
    }

    public ClientConfig(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String servicePathList, ServerVerifyHandler serverVerifyHandler) {
        this(connectString,connectionTimeout,sessionTimeout,namespace, servicePathList);
    }

    public ClientConfig(ZookeeperConfig zookeeperConfig, String servicePathList, ServerVerifyHandler serverVerifyHandler) {
        super(zookeeperConfig.getConnectString(),zookeeperConfig.getConnectionTimeout(),zookeeperConfig.getSessionTimeout(),zookeeperConfig.getNamespace());
        this.servicePathList = servicePathList;
    }
    public ClientConfig(ZookeeperConfig zookeeperConfig, String servicePathList) {
        this(zookeeperConfig, servicePathList,null);
    }
    public ClientConfig() {

    }

    public String getServicePathList() {
        return servicePathList;
    }

    public void setServicePathList(String servicePathList) {
        this.servicePathList = servicePathList;
    }
}
