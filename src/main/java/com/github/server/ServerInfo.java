package com.github.server;

import com.github.service.ServiceConfiguration;

import java.util.HashMap;

/**
 * Created by tumingjian on 2017/3/12.
 */
public class ServerInfo {
    /**
     * 服务注册在zookeeper 上的path值
     */
    private String path;
    /**
     * 当前节点名
     */
    private String nodeName;
    /**
     * 当前命名空间
     */
    private String namespace;
    /**
     * 当前服务名
     */
    private String serviceName;
    /**
     * 当前服务的配置信息
     */
    private ServiceConfiguration serviceConfig;

    public ServerInfo(String path, String nodeName, String namespace, String serviceName, ServiceConfiguration serviceConfig) {
        this.path = path;
        this.nodeName = nodeName;
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.serviceConfig = serviceConfig;
    }

    public ServerInfo() {
    }

    public String getPath() {
        return path;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ServiceConfiguration getServiceConfig() {
        return serviceConfig;
    }

}
