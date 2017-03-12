package com.github.zookeeper;

/**
 * Created by tumingjian on 2017/3/11.
 */
public class ZookeeperConfiguration implements ZookeeperConfig {
    /**
     *     zookeeper服务器列表,格式:   IP:PORT,IP:PORT
     */
    private String connectString;
    /**
     * 连接超时时间
     */

    private int connectionTimeout=30;
    /**
     * 会话超时时间
     */
    private int sessionTimeout=2;
    /**
     * 前置路径,可以为空,如果设置了该值,那么后续的所有操作都只针对该节点或该节点的子节点下.
     */
    private String namespace;

    public ZookeeperConfiguration(String connectString, int connectionTimeout, int sessionTimeout, String namespace) {
        this.connectString = connectString;
        this.connectionTimeout = connectionTimeout;
        this.sessionTimeout = sessionTimeout;
        this.namespace = namespace;
    }
    public ZookeeperConfiguration(){

    }
    @Override
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
