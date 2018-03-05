package com.zookeeper.service.client;


import java.util.List;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ServiceWatchInvocation {
    private String servicePath;
    private String sequenceNode;
    private List<ServerInfo> activeServerList;

    public String getServicePath() {
        return servicePath;
    }


    public List<ServerInfo> getActiveServerList() {
        return activeServerList;
    }

    public ServiceWatchInvocation(String servicePath,String sequenceNode, List<ServerInfo> activeServerList) {
        this.servicePath = servicePath;
        this.sequenceNode=sequenceNode;
        this.activeServerList = activeServerList;
    }

    public String getSequenceNode() {
        return sequenceNode;
    }
}
