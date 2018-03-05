package com.zookeeper.service.server;

import java.util.HashMap;

/**
 * 服务配置信息,如果还想添加除了ip,port之外的其他信息,可以用put函数添加
 *@author tumingjian
 */
public class ServiceNodeData extends HashMap<String, Object> {
    private final static String HOST_KEY = "ip";
    private final static String PORT_KEY = "port";
    private final static String ONLINE_TIME="onlineTime";
    public String getHost() {
        return (String) get(HOST_KEY);
    }

    public ServiceNodeData() {

    }

    /**
     * 指定Ip和port
     * @param host
     * @param port
     */
    public ServiceNodeData(String host, String port) {
        this.put(HOST_KEY, host);
        this.put(PORT_KEY, port);
        this.put(ONLINE_TIME,System.currentTimeMillis());
    }

    public void setHost(String host) {
        this.put(HOST_KEY, host);
    }

    public String getPort() {
        return (String) this.get(PORT_KEY);
    }

    public void setPort(String port) {
        this.put(PORT_KEY, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null){
            return false;
        }
        if (this == obj){
            return true;
        }
        if (obj instanceof ServiceNodeData) {
            ServiceNodeData item = (ServiceNodeData) obj;
            if (item.getHost().equals(this.getHost()) && item.getPort().equals(this.getPort())) {
                return true;
            }
        }
        return false;
    }



}
