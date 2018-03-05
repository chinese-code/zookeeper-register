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
    private String servicePathList;
    public ClientConfiguration(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String servicePathList) {
        super(connectString, connectionTimeout, sessionTimeout, namespace);
        this.servicePathList = servicePathList;
    }

    public ClientConfiguration(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String servicePathList, ServerVerifyHandler serverVerifyHandler) {
        this(connectString,connectionTimeout,sessionTimeout,namespace, servicePathList);
    }

    public ClientConfiguration(ZookeeperConfig zookeeperConfig, String servicePathList, ServerVerifyHandler serverVerifyHandler) {
        super(zookeeperConfig.getConnectString(),zookeeperConfig.getConnectionTimeout(),zookeeperConfig.getSessionTimeout(),zookeeperConfig.getNamespace());
        this.servicePathList = servicePathList;
    }
    public ClientConfiguration(ZookeeperConfig zookeeperConfig, String servicePathList) {
        this(zookeeperConfig, servicePathList,null);
    }
    public ClientConfiguration() {

    }

    public String getServicePathList() {
        return servicePathList;
    }

    public void setServicePathList(String servicePathList) {
        this.servicePathList = servicePathList;
    }
}
