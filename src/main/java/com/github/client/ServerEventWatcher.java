package com.github.client;

import com.github.server.ServerInfo;
import com.github.service.ServiceConfiguration;

/**
 * 通知对象
 */
public interface ServerEventWatcher {
    /**
     * 有服务器上线时触发
     * @param manager
     * @param currentOnlineServer 上线的服务器
     */
    void online(HostManager manager, ServerInfo currentOnlineServer);

    /**
     * 有服务离线时触发
     * @param manager
     * @param currentOfflineServer 离线的服务器
     */
    void offline(HostManager manager, ServerInfo currentOfflineServer);

    /**
     * 有服务器更新配置时触发
     * @param manager
     * @param oldServerConfig  老的配置信息
     * @param newServerConfig  新的配置信息
     */
    void update(HostManager manager, ServerInfo oldServerConfig, ServerInfo newServerConfig);
}
