package com.github.client;

import com.alibaba.fastjson.JSON;
import com.github.service.ServiceNodeData;
import com.github.utils.JSONUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import com.github.server.ServiceRegister;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ServiceClient implements ActiveServerInfo {
    private static Logger logger = Logger.getLogger(ServiceRegister.class);
    /**
     * 客户端配置
     */
    private ClientConfiguration config;
    /**
     * 当前活跃的主机列表
     */
    private volatile Map<String, ServerInfo> activeServerMap = new HashMap<String, ServerInfo>();
    /**
     * com.github.zookeeper com.github.client
     */
    final private CuratorFramework client;
    /**
     * 服务的相应事件触发时,通知处理的handler列表
     */
    private List<ServiceHostEventWatcher> eventHandlers = new ArrayList<ServiceHostEventWatcher>();
    /**
     * 服务器验证处理者.
     */
    private ServerVerifyHandler serverVerifyHandler;

    public ServiceClient(ClientConfiguration config) {
        /**
         * 初始化zookeeper client,并注册监听器
         */
        this.config = config;
        this.client = CuratorFrameworkFactory.builder()
                .canBeReadOnly(false)
                .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
                .defaultData(null)
                .namespace(config.getNamespace())
                .connectString(config.getConnectString())
                .sessionTimeoutMs(config.getSessionTimeout())
                .connectionTimeoutMs(config.getConnectionTimeout()).build();
        this.client.start();
        this.watch();
    }

    /**
     * 关闭zookeeper com.github.client
     */
    public void close() {
        this.client.close();
    }

    /**
     * 可重复的监听当前服务下所有的服务上线,下线,或配置更新事件.
     */
    protected void watch() {
        activeServerMap = initServerMap();
        InnerCuratorWatcher innerCuratorWatcher = new InnerCuratorWatcher(this);
        watchNode(config.getServicePath(), client, innerCuratorWatcher);
    }

    /**
     * 向列表中添加一个服务事件通知处理器
     *
     * @param watcher
     */
    public void addWatcher(ServiceHostEventWatcher watcher) {
        eventHandlers.add(watcher);
    }

    /**
     * 向列表中移除一个服务事件通知处理器
     *
     * @param watcher
     */
    public void removeWatcher(ServiceHostEventWatcher watcher) {
        eventHandlers.remove(watcher);
    }

    /**
     * 初始化活跃主机列表
     *
     * @return
     */
    private Map<String, ServerInfo> initServerMap() {
        HashMap<String, ServerInfo> map = new HashMap<String, ServerInfo>();
        List<String> childs = retryGetChildren(config.getServicePath());
        for (String child : childs) {
            String path = config.getServicePath() + "/" + child;
            try {
                ServerInfo serverData = getServerInfo(path);
                if (serverData != null) {
                    if (this.serverVerifyHandler == null || this.serverVerifyHandler.verify(serverData)) {
                        map.put(child, serverData);
                    } else {
                        logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverconfig:{1}", path, JSON.toJSONString(serverData)));
                    }
                } else {
                    logger.error(MessageFormat.format("serverconfig is null,path:{0}", path));
                }
            } catch (Exception e) {
                logger.error(MessageFormat.format("add serverActiveList error,skip path:{0}", path));
            }
        }
        return map;
    }

    /**
     * 支持最大可重试3次来获取某个PATH下的所有子节点列表
     *
     * @param path
     * @return
     */
    private List<String> retryGetChildren(String path) {
        for (int i = 0; i < 3; i++) {
            try {
                List<String> list = this.client.getChildren().forPath(path);
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException(MessageFormat.format("get children fail,path:{0}", path));
    }

    /**
     * 支持最大可重试3次来获取某个PATH上的数据
     *
     * @param path
     * @return
     */
    private byte[] retryGetDate(String path) {
        for (int i = 0; i < 3; i++) {
            try {
                byte[] bytes = this.client.getData().forPath(path);
                return bytes;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException(MessageFormat.format("get data fail,path:{0}", path));
    }

    /**
     * 注册Watcher到zookeeper相应的结点,监听事件
     *
     * @param servicePath
     * @param client
     * @param curatorWatcher
     */
    private void watchNode(String servicePath, CuratorFramework client, CuratorWatcher curatorWatcher) {
        try {
            List<String> childs = client.getChildren().usingWatcher(curatorWatcher).forPath(servicePath);
            for (String childNode : childs) {
                String path = servicePath + "/" + childNode;
                client.checkExists().usingWatcher(curatorWatcher).forPath(path);
                client.getData().usingWatcher(curatorWatcher).forPath(path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取活跃服务器列表
     *
     * @return
     */
    @Override
    public Collection<ServerInfo> getActiveServers() {
        return activeServerMap.values();
    }
    @Override
    public Map<String,ServerInfo> getActiveSeverMap(){
        return activeServerMap;
    }
    @Override
    public boolean isActiveServer() {
        return !activeServerMap.isEmpty();
    }

    /**
     * 强制更活跃服务器列表
     */
    public void refreshActiveServers() {
        this.activeServerMap = initServerMap();
    }


    private ServerInfo getServerInfo(String path) {
        byte[] bytes = retryGetDate(path);
        ServerInfo serverInfo=JSON.parseObject(new String(bytes), ServerInfo.class);
        return serverInfo;
    }

    public void setServerVerifyHandler(ServerVerifyHandler verifyHandler) {
        serverVerifyHandler = verifyHandler;
    }

    public ServerVerifyHandler getServerVerifyHandler() {
        return serverVerifyHandler;
    }

    /**
     * Zookeeper Watcher的实现类,实现了一个Watcher事件转换为服务器上线,下线和服务器配置更新事件触发时通知
     * 到多个ServerEventWatcher事件上.
     */
    private class InnerCuratorWatcher implements CuratorWatcher {
        final private String servicePath;
        final private List<ServiceHostEventWatcher> handlers;
        final private ServiceClient serviceClient;

        public InnerCuratorWatcher(ServiceClient serviceClient) {
            this.serviceClient = serviceClient;
            this.servicePath = config.getServicePath();
            this.handlers = serviceClient.eventHandlers;

        }

        /**
         * zookeeper监听事件处理.
         *
         * @param event
         * @throws Exception
         */
        @Override
        public void process(WatchedEvent event) throws Exception {
            if (event.getType() == Watcher.Event.EventType.None) {
                logger.info("Service监视器注册失败!");
            } else {
                int subEndIndex=servicePath.length()+1;
                String path = event.getPath();
                logger.info("watchedEvent:" + event);
                //server offline
                if (event.getType() == Watcher.Event.EventType.NodeDeleted && path.contains(servicePath)) {
                    String child = path.substring(subEndIndex);
                    ServerInfo serverInfo = activeServerMap.get(child);
                    logger.info(MessageFormat.format("服务器离线,NODE:{0},info:{1}", child, JSON.toJSONString(serverInfo)));
                    activeServerMap.remove(child);
                    if (handlers != null) {
                        for (ServiceHostEventWatcher watcher : handlers) {
                            watcher.offline(serviceClient, serverInfo);
                        }
                    }
                    //server update
                } else if (event.getType() == Watcher.Event.EventType.NodeDataChanged && !path.contains(servicePath)) {
                    String child = path.substring(subEndIndex);
                    ServerInfo oldServerInfo = activeServerMap.get(child);
                    ServerInfo newServerInfo = getServerInfo(event.getPath());
                    //如果更新之后,服务器未通过验证,那么新的服务不会被添加到活跃列表,旧的服务依然会被移除,并触发离线事件.
                    if (serviceClient.serverVerifyHandler == null || serviceClient.serverVerifyHandler.verify(newServerInfo)) {
                        activeServerMap.put(child, newServerInfo);
                        logger.info(MessageFormat.format("服务器更新,NODE:{0},oldInfo:{1},newInfo:{2}", child, JSON.toJSONString(oldServerInfo), JSON.toJSONString(newServerInfo)));
                        if (handlers != null) {
                            for (ServiceHostEventWatcher watcher : handlers) {
                                watcher.update(serviceClient, oldServerInfo, newServerInfo);
                            }
                        }
                    } else {
                        logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverconfig:{1}", event.getPath(), JSON.toJSONString(newServerInfo)));
                        activeServerMap.remove(child);
                        logger.info(MessageFormat.format("服务器离线,NODE:{0},info:{1}", child, JSON.toJSONString(newServerInfo)));
                        if (handlers != null) {
                            for (ServiceHostEventWatcher watcher : handlers) {
                                watcher.offline(serviceClient, newServerInfo);
                            }
                        }
                    }
                    //server online
                } else if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged && path.contains(servicePath)) {
                    List<String> childrens = retryGetChildren(event.getPath());
                    for (String child : childrens) {
                        ServerInfo serverInfo = getServerInfo(servicePath + "/" + child);
                        if (activeServerMap.get(child) == null) {
                            //只有在通过验证时,服务才会加入活跃列表,并触发上线事件.
                            if (serviceClient.serverVerifyHandler == null || serviceClient.serverVerifyHandler.verify(serverInfo)) {
                                activeServerMap.put(child, serverInfo);
                                logger.info(MessageFormat.format("新服务器上线,NODE:{0},info:{1}", child, JSON.toJSONString(serverInfo)));
                                if (handlers != null) {
                                    for (ServiceHostEventWatcher watcher : handlers) {
                                        watcher.online(serviceClient, serverInfo);
                                    }
                                }
                            } else {
                                logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverconfig:{1}",servicePath + "/" + child, JSON.toJSONString(serverInfo)));
                            }
                        }
                    }
                }
            }
            /**
             *由于zookeeper Watcher一次注册只能一次监听,所以需要再次注册新的监听器
             */
            watchNode(servicePath, client, this);
        }
    }

    public ClientConfiguration getConfig() {
        return config;
    }

    public List<ServiceHostEventWatcher> getEventHandlers() {
        return eventHandlers;
    }
}
