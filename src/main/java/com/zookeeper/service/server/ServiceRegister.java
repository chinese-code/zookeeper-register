package com.zookeeper.service.server;

import com.alibaba.fastjson.JSON;
import com.zookeeper.service.utils.JsonUtils;
import com.zookeeper.service.zookeeper.ZookeeperConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ServiceRegister {
    private static Logger logger= LoggerFactory.getLogger(ServiceRegister.class);
    /**
     *
     */
    final private CuratorFramework client;
    /**
     * 服务配置信息
     */
    private ZookeeperConfiguration zookeeperConfig;
    /**
     * 当前服务信息ServerInfo在zookeeper上挂载的节点PATH
     */
    private String currentServerInfoPath;
    /**
     * 当前服务信息ServerInfo在zookeeper节点上的名称
     */
    private String currentServerInfoNodeName;
    /**
     * 服务器配置
     */
    private ServiceConfig serverInfo;
    private ServiceNodeData serverData;
    /**
     * 服务是否已上线.
     */
    private volatile boolean online = false;

    public ServiceRegister(ZookeeperConfiguration config, ServiceConfig serverInfo, ServiceNodeData serverData) {
        this.zookeeperConfig = config;
        this.serverInfo=serverInfo;
        this.serverData=serverData;
        this.client = CuratorFrameworkFactory.builder()
                .canBeReadOnly(false)
                .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
                .defaultData(null)
                .namespace(config.getNamespace())
                .connectString(config.getConnectString())
                .sessionTimeoutMs(config.getSessionTimeout())
                .connectionTimeoutMs(config.getConnectionTimeout()).build();
        this.client.start();
    }

    /**
     * 根据配置信息注册当前服务
     */
    public void online() {
        if (online) {
            throw new RuntimeException("当前服务已经在线,不能重复上线");
        }
        String path = serverInfo.getPath();
        String data = JSON.toJSONString(serverData);
        try {
            Stat stat = this.client.checkExists().forPath(path);
            if (stat == null) {
                this.client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }
            byte[] bytes = data.getBytes("utf-8");
            this.currentServerInfoPath = this.client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path+"/_",bytes);
            logger.info("服务注册成功,namespace:"+zookeeperConfig.getNamespace());
            logger.info("服务注册成功,path:"+this.currentServerInfoPath);
            logger.info("服务注册成功,serverdata:"+data);
            online = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 服务器暂时离线,但不关闭zookeeper com.github.client,如果之后需要重新上线,再执行online()
     */
    public void offline() {
        try {
            this.client.delete().forPath(currentServerInfoPath);
            online = false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 服务是否可用
     * @return
     */
    public boolean isOffline() {
        return !online;
    }

    /**
     * 关闭zookeeper com.github.client
     */
    public void close() {
        this.client.close();
    }


    /**
     * 更新当前服务器配置信息
     *
     * @param serverData
     */
    public void updateCurrent(ServiceNodeData serverData) {
        if (currentServerInfoNodeName != null && currentServerInfoPath != null) {
            try {
                if (serverInfo.getServiceName().equals(serverInfo.getServiceName())) {
                    this.client.setData().forPath(currentServerInfoPath, JSON.toJSONString(serverData).getBytes("utf-8"));
                } else {
                    throw new RuntimeException("新的serviceName和原有的serviceName不一致,无法更新服务器信息");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new NullPointerException("error,currentServerInfoPath is null");
        }
    }

    /**
     * 根据zookeeper path获取data信息
     * @param path
     * @return
     * @throws Exception
     */
    private ServiceNodeData getServerInfo(String path) throws Exception {
        byte[] bytes = this.client.getData().forPath(currentServerInfoPath);
        return JsonUtils.parse(new String(bytes,"utf-8"), ServiceNodeData.class);
    }
}
