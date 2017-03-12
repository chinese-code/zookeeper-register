package com.github.service;

import java.util.HashMap;

/**
 * 服务配置信息,如果还想添加除了ip,port之外的其他信息,可以用put函数添加
 *
 */
public class ServiceConfiguration extends HashMap<String, Object> {
    private final static String HOST_KEY = "ip";
    private final static String PORT_KEY = "port";
    public String getHost() {
        return (String) get(HOST_KEY);
    }

    public ServiceConfiguration() {

    }

    /**
     * 指定Ip和port
     * @param host
     * @param port
     */
    public ServiceConfiguration(String host, String port) {
        this.put(HOST_KEY, host);
        this.put(PORT_KEY, port);
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
        if (obj == null) return false;
        if (obj == obj) return true;
        if (obj instanceof ServiceConfiguration) {
            ServiceConfiguration item = (ServiceConfiguration) obj;
            if (item.getHost() == this.getHost() && item.getPort() == this.getPort()) {
                return true;
            }
        }
        return false;
    }


}
