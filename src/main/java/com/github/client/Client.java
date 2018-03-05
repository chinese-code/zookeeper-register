package com.github.client;

import com.alibaba.fastjson.JSON;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class Client {
    private static Logger logger = Logger.getLogger(ServiceRegister.class);
    /**
     * 客户端配置
     */
    private ClientConfiguration config;
    /**
     * 当前活跃的主机列表
     */
    private volatile Map<String, ConcurrentHashMap<String, ServerInfo>> activeServerMap = new HashMap<String, ConcurrentHashMap<String, ServerInfo>>();
    /**
     * com.github.zookeeper com.github.client
     */
    final private CuratorFramework client;
    /**
     * 服务的相应事件触发时,通知处理的handler列表
     */
    private List<ServiceEventWatcher> eventHandlers = new ArrayList<ServiceEventWatcher>();
    /**
     * 服务器验证处理者.
     */
    private ServerVerifyHandler serverVerifyHandler;

    /**
     * @param config
     */
    private Set<String> servicePathList = new HashSet<String>();

    public Client(ClientConfiguration config) {
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
        this.servicePathList = new HashSet<String>();
        this.servicePathList.addAll(Arrays.asList(config.getServicePathList().split(";")));
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
        for (String servicePath : servicePathList) {
            InnerCuratorWatcher innerCuratorWatcher = new InnerCuratorWatcher(this,servicePath);
            watchNode(servicePath, client, innerCuratorWatcher);
        }
    }

    /**
     * 向列表中添加一个服务事件通知处理器
     *
     * @param watcher
     */
    public void addWatcher(ServiceEventWatcher watcher) {
        eventHandlers.add(watcher);
    }

    /**
     * 向列表中移除一个服务事件通知处理器
     *
     * @param watcher
     */
    public void removeWatcher(ServiceEventWatcher watcher) {
        eventHandlers.remove(watcher);
    }

    /**
     * 初始化活跃主机列表
     *
     * @return
     */
    private Map<String, ConcurrentHashMap<String, ServerInfo>> initServerMap() {
        Map<String, ConcurrentHashMap<String, ServerInfo>> result = new HashMap<String, ConcurrentHashMap<String, ServerInfo>>();
        for (String servicePath : servicePathList) {
            List<String> childs = retryGetChildren(servicePath);
            ConcurrentHashMap<String, ServerInfo> map = new ConcurrentHashMap<String, ServerInfo>();
            for (String child : childs) {
                String path = servicePath + "/" + child;
                try {
                    ServerInfo serverData = getServerInfo(path);
                    if (serverData != null) {
                        if (this.serverVerifyHandler == null || this.serverVerifyHandler.verify(serverData)) {
                            map.put(child, serverData);
                        } else {
                            logger.error(MessageFormat.format("服务器未通过验证,path:{0},servicepath:{1}", path, JSON.toJSONString(serverData)));
                        }
                    } else {
                        logger.error(MessageFormat.format("serverconfig is null,path:{0}", path));
                    }
                } catch (Exception e) {
                    logger.error(MessageFormat.format("add serverActiveList error,skip path:{0}", path));
                }
            }
            result.put(servicePath, map);
        }
        return result;
    }

    /**
     * 支持最大可重试3次来获取某个PATH下的所有子节点列表
     *
     * @param servicePath
     * @return
     */
    private List<String> retryGetChildren(String servicePath) {
        for (int i = 0; i < 3; i++) {
            try {
                List<String> list = this.client.getChildren().forPath(servicePath);
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException(MessageFormat.format("get children fail,path:{0}", servicePath));
    }

    /**
     * 支持最大可重试3次来获取某个PATH上的数据
     *
     * @param servicePath
     * @return
     */
    private byte[] retryGetDate(String servicePath) {
        for (int i = 0; i < 3; i++) {
            try {
                byte[] bytes = this.client.getData().forPath(servicePath);
                return bytes;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException(MessageFormat.format("get data fail,path:{0}", servicePath));
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
    public Collection<ServerInfo> getActiveServers(String servicePath) {
        ConcurrentHashMap<String, ServerInfo> service = activeServerMap.get(servicePath);
        if (service != null) {
            return service.values();
        }
        return null;
    }

    public Map<String, ServerInfo> getActiveSeverMap(String servicePath) {
        return activeServerMap.get(servicePath);
    }

    public boolean isActiveServer(String servicePath) {
        if (activeServerMap.get(servicePath) == null) return false;
        return !activeServerMap.get(servicePath).isEmpty();
    }

    /**
     * 强制更活跃服务器列表
     */
    public void refreshActiveServers() {
        this.activeServerMap = initServerMap();
    }


    private ServerInfo getServerInfo(String serviceNodePath) {
        byte[] bytes = retryGetDate(serviceNodePath);
        ServerInfo serverInfo = JSON.parseObject(new String(bytes), ServerInfo.class);
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
        final private List<ServiceEventWatcher> handlers;
        final private Client serviceClient;

        public InnerCuratorWatcher(Client serviceClient, String servicePath) {
            this.serviceClient = serviceClient;
            this.servicePath = servicePath;
            this.handlers = serviceClient.eventHandlers;

        }

        public ClientConfiguration getConfig() {
            return config;
        }

        public List<ServiceEventWatcher> getEventHandlers() {
            return eventHandlers;
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
                int subEndIndex = servicePath.length() + 1;
                String path = event.getPath();
                logger.info("watchedEvent:" + event);
                //server offline
                if (event.getType() == Watcher.Event.EventType.NodeDeleted && path.contains(servicePath)) {
                    String child = path.substring(subEndIndex);
                    ServerInfo serverInfo = activeServerMap.get(servicePath).get(child);
                    if(logger.isDebugEnabled()){
                        logger.info(MessageFormat.format("服务器离线,NODE:{0},info:{1}", child, JSON.toJSONString(serverInfo)));
                    }
                    activeServerMap.get(path).remove(child);
                    if (handlers != null) {
                        for (ServiceEventWatcher watcher : handlers) {
                            ArrayList<ServerInfo> serverInfos = toArrayList();
                            watcher.offline(new ServiceWatchInvocation(servicePath,serverInfos), serverInfo);
                        }
                    }
                    //server update
                } else if (event.getType() == Watcher.Event.EventType.NodeDataChanged && path.contains(servicePath)) {
                    String child = path.substring(subEndIndex);
                    ServerInfo oldServerInfo = activeServerMap.get(servicePath).get(child);
                    ServerInfo newServerInfo = getServerInfo(event.getPath());
                    //如果更新之后,服务器未通过验证,那么新的服务不会被添加到活跃列表,旧的服务依然会被移除,并触发离线事件.
                    if (serviceClient.serverVerifyHandler == null || serviceClient.serverVerifyHandler.verify(newServerInfo)) {
                        activeServerMap.get(servicePath).put(child, newServerInfo);
                        if(logger.isDebugEnabled()){
                            logger.debug(MessageFormat.format("服务器更新,NODE:{0},oldInfo:{1},newInfo:{2}", child, JSON.toJSONString(oldServerInfo), JSON.toJSONString(newServerInfo)));

                        }
                        if (handlers != null) {
                            for (ServiceEventWatcher watcher : handlers) {
                                ArrayList<ServerInfo> serverInfos = toArrayList();
                                watcher.update(new ServiceWatchInvocation(servicePath,serverInfos), oldServerInfo, newServerInfo);
                            }
                        }
                    } else {
                        logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverconfig:{1}", event.getPath(), JSON.toJSONString(newServerInfo)));
                        activeServerMap.get(servicePath).remove(child);
                        logger.debug(MessageFormat.format("服务器离线,NODE:{0},info:{1}", child, JSON.toJSONString(newServerInfo)));
                        if (handlers != null) {
                            for (ServiceEventWatcher watcher : handlers) {
                                ArrayList<ServerInfo> serverInfos = toArrayList();
                                watcher.offline(new ServiceWatchInvocation(servicePath,serverInfos), newServerInfo);
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
                                activeServerMap.get(servicePath).put(child, serverInfo);
                                if(logger.isDebugEnabled()){
                                    logger.debug(MessageFormat.format("新服务器上线,NODE:{0},info:{1}", child, JSON.toJSONString(serverInfo)));
                                }
                                if (handlers != null) {
                                    for (ServiceEventWatcher watcher : handlers) {
                                        ArrayList<ServerInfo> serverInfos = toArrayList();
                                        watcher.online(new ServiceWatchInvocation(servicePath,serverInfos), serverInfo);
                                    }
                                }
                            } else {
                                logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverconfig:{1}", servicePath + "/" + child, JSON.toJSONString(serverInfo)));
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

        private ArrayList<ServerInfo> toArrayList() {
            Collection<ServerInfo> values = activeServerMap.get(servicePath).values();
            ArrayList<ServerInfo> serverInfos = new ArrayList<ServerInfo>();
            serverInfos.addAll(values);
            return serverInfos;
        }
    }

}
