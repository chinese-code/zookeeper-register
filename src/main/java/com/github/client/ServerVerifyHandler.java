package com.github.client;

import com.github.server.ServerInfo;
import com.github.service.ServiceConfiguration;

/**
 * Created by tumingjian on 2017/3/12.
 * 验证服务器是否合法,会在三个地方调用该接口,在服务器更新,上线,和ServiceClientManager初始化活跃列表时调用,防止非法服务器注册,造成不可
 * 预料的后果.
 */
public interface ServerVerifyHandler {
    /**
     * 是否验证通过
     * @param serverInfo
     * @return
     */
    boolean verify(ServerInfo serverInfo);
}