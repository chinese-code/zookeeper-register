package com.github.client;

import com.github.server.ServerInfo;
import com.github.service.ServiceConfiguration;

import java.util.Collection;

/**
 * Created by tumingjian on 2017/3/12.
 */
public interface HostManager {
    /**
     * 获取当前可用的服务器列表和配置信息
     * @return
     */
    Collection<ServerInfo> getActiveServerInfoList();

    /**
     * 强制刷新可用服务列表.
     */
    public void refreshActiveServerInfoList();
}
