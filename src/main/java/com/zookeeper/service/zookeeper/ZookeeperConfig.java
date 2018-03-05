package com.zookeeper.service.zookeeper;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public interface ZookeeperConfig {
    /**
     * 连接超时时间
     * @return
     */
    int getConnectionTimeout();

    /**
     * zookeeper连接字符串,多个服务器地址用逗号分隔.ip:端口,ip:端口
     * @return
     */
    String getConnectString();

    /**
     * session超时时间.当连接到zookeeper时,超过这个毫秒数没有心跳,将视为连接断开.
     * @return
     */

    int getSessionTimeout();

    /**
     * 命名空间,所有的操作都是在/命令空间目录下,相当于为所有操作强制指定一个根目录
     * @return
     */

    String getNamespace();
}
