package com.github;

import com.alibaba.fastjson.JSON;
import com.github.client.*;
import com.github.zookeeper.ZookeeperConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ZookeeperServiceClientDemo {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceClientDemo.class);
    //zookeeper 服务器列表
    String connectString = "127.0.0.1:2181";
    private String servicePathList = "/com.zhongjinlianhe/base/Service.SnowFlakeGenId;/com.zhongjinlianhe/base/Service.UserCenter";
    private String sonwFlakeGenIdServicePath = "/com.zhongjinlianhe/base/Service.SnowFlakeGenId";
    private String userCenterServicePath = "/com.zhongjinlianhe/base/Service.UserCenter";
    int connectionTimeout = 30000;
    int sessionTimeout = 2000;
    //curator-framework zookepper 命名空间
    String namespace = "test";

    /**
     * 创建一个服务的client,并获取这个服务的所有可用主机列表
     *
     * @throws Exception
     */
    @Test
    public void simpleClient() throws Exception {
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                connectionTimeout, sessionTimeout, namespace);
        ClientConfiguration config = new ClientConfiguration(zookeeperConfiguration, servicePathList);
        Client client = new Client(config);
        client.addWatcher(new ServiceEventWatcher() {
            public void online(ServiceWatchInvocation invoker, ServerInfo onlineServer) {
                logger.info("servicePath:"+invoker.getServicePath());
                logger.info("服务器上线:"+JSON.toJSONString(onlineServer));
                logger.info("info:"+JSON.toJSONString(invoker.getActiveServerList()));
            }

            public void offline(ServiceWatchInvocation invoker, ServerInfo offlineServer) {
                logger.info("servicePath:"+invoker.getServicePath());
                logger.info("服务器离线:"+JSON.toJSONString(offlineServer));
                logger.info("info:"+JSON.toJSONString(invoker.getActiveServerList()));
            }

            public void update(ServiceWatchInvocation invoker, ServerInfo oldServerInfo, ServerInfo newServerInfo) {
                logger.info("servicePath:"+invoker.getServicePath());
                logger.info("服务器更新旧的:"+JSON.toJSONString(oldServerInfo));
                logger.info("服务器更新新的:"+JSON.toJSONString(newServerInfo));
                logger.info("info:"+JSON.toJSONString(invoker.getActiveServerList()));
            }
        });
        //获取已注册的服务器列表
        Collection<ServerInfo> list = client.getActiveServers(sonwFlakeGenIdServicePath);
        logger.info("当前活跃主机:" + JSON.toJSONString(list));
        //获取已注册的服务器列表
        Collection<ServerInfo> list2 = client.getActiveServers(userCenterServicePath);
        logger.info("当前活跃主机:" + JSON.toJSONString(list2));

        while (true) {
            Thread.sleep(1000);
        }
    }
}
