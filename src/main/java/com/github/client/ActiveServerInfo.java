package com.github.client;



import java.util.Collection;
import java.util.Map;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public interface ActiveServerInfo {
    /**
     * 获取当前可用的服务器列表和配置信息
     * @return
     */
    Collection<ServerInfo> getActiveServers();

    /**
     * 获取活跃主机Map,Key为zookeeper中的最后一个序列节点名
     * @return
     */
    public Map<String,ServerInfo> getActiveSeverMap();
    /**
     * 当前服务是否有活跃列表.
     */
    boolean isActiveServer();

}
