package com.github.client;

import com.github.service.ServiceNodeData;

import java.util.Collection;

/**
 * 通知对象
 * @author tumingjian
 */
public interface ServiceHostEventWatcher {
    /**
     * 有服务器上线时触发
     * @param activeServers
     * @param currentOnlineServer 上线的服务器
     */
    void online(ActiveServerInfo activeServerInfo, ServerInfo currentOnlineServer);

    /**
     * 有服务离线时触发
     * @param activeServers
     * @param currentOfflineServer 离线的服务器
     */
    void offline(ActiveServerInfo activeServerInfo, ServerInfo currentOfflineServer);

    /**
     * 有服务器更新配置时触发
     * @param activeServers
     * @param oldServerConfig  老的配置信息
     * @param newServerConfig  新的配置信息
     */
    void update(ActiveServerInfo activeServerInfo, ServerInfo oldServerConfig, ServerInfo newServerConfig);
}
