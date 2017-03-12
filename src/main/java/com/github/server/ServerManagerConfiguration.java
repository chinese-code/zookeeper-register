package com.github.server;

import com.github.service.ServiceConfiguration;
import com.github.zookeeper.ZookeeperConfig;
import com.github.zookeeper.ZookeeperConfiguration;

/**
 * 服务管理者配置信息
 */
public class ServerManagerConfiguration extends ZookeeperConfiguration implements ServerManagerConfig {
    /**
     * 服务配置信息
     */
    private ServiceConfiguration serviceConfiguration;
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 最基本的构造函数
     * @param connectString zookeeper server list
     * @param connectionTimeout  zookeeper server connectionTimeout
     * @param sessionTimeout zookeeper server sessionTimeout
     * @param namespace  namespace
     * @param serviceName 服务名
     * @param host  当前主机的可用IP地址
     * @param port  当前服务的端口
     */
    public ServerManagerConfiguration(String connectString, int connectionTimeout, int sessionTimeout, String namespace, String serviceName, String host, String port) {
        super(connectString, connectionTimeout, sessionTimeout, namespace);
        this.serviceConfiguration = new ServiceConfiguration(port, host);
        this.serviceName = serviceName;
    }

    public ServerManagerConfiguration(ZookeeperConfig zookeeperConfig, String serviceName, ServiceConfiguration serviceConfiguration) {
        super(zookeeperConfig.getConnectString(), zookeeperConfig.getConnectionTimeout(), zookeeperConfig.getSessionTimeout()
                , zookeeperConfig.getNamespace());
        this.serviceConfiguration = serviceConfiguration;
        this.serviceName = serviceName;
    }

    public ServerManagerConfiguration() {

    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public String getHost() {
        return serviceConfiguration.getHost();
    }

    @Override
    public String getPort() {
        return serviceConfiguration.getPort();
    }

    public ServiceConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }


}
