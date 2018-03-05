package com.github.client;

/**
 * 通知对象
 * @author tumingjian
 */
public interface ServiceEventWatcher {
    /**
     * 有服务器上线时触发
     * @param invoker
     * @param onlineServer 上线的服务器
     */
    void online(ServiceWatchInvocation invoker, ServerInfo onlineServer);

    /**
     * 有服务离线时触发
     * @param invoker
     * @param offlineServer 离线的服务器
     */
    void offline(ServiceWatchInvocation invoker, ServerInfo offlineServer);

    /**
     * 有服务器更新配置时触发
     * @param invoker
     * @param oldServerInfo  老的配置信息
     * @param newServerInfo  新的配置信息
     */
    void update(ServiceWatchInvocation invoker, ServerInfo oldServerInfo, ServerInfo newServerInfo);
}
