package com.zookeeper.service.client;

import com.zookeeper.service.server.ServiceNodeData;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 * 验证服务器是否合法,会在三个地方调用该接口,在服务器更新,上线,和ServiceClient初始化活跃列表时调用,防止非法服务器注册,造成不可
 * 预料的后果.
 */
public interface ServerVerifyHandler {
    /**
     * 是否验证通过
     * @param serverData
     * @return
     */
    boolean verify(ServiceNodeData serverData);
}
