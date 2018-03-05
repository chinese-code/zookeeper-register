package com.zookeeper.service.client;

import com.alibaba.fastjson.JSON;
import com.zookeeper.service.server.ServiceRegister;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;


import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ServiceClient {
    private static Logger logger = Logger.getLogger(ServiceRegister.class);
    /**
     * 客户端配置
     */
    private ClientConfig clientConfig;
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
     * @param clientConfig
     */
    private Set<String> servicePathList = new HashSet<String>();
    private static final int MAX_RETRY_TIMES = 3;

    public ServiceClient(ClientConfig clientConfig) {
        /**
         * 初始化zookeeper client,并注册监听器
         */
        this.clientConfig = clientConfig;
        this.client = CuratorFrameworkFactory.builder()
                .canBeReadOnly(false)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .defaultData(null)
                .namespace(clientConfig.getNamespace())
                .connectString(clientConfig.getConnectString())
                .sessionTimeoutMs(clientConfig.getSessionTimeout())
                .connectionTimeoutMs(clientConfig.getConnectionTimeout()).build();
        this.servicePathList = new HashSet<String>();
        this.servicePathList.addAll(Arrays.asList(clientConfig.getServicePathList().split(",")));
        this.servicePathList= this.servicePathList.stream().map(i->i+"/host").collect(Collectors.<String>toSet());
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
        final ServerVerifyHandler serverVerifyHandler = this.serverVerifyHandler;
        final List<ServiceEventWatcher> watchers = this.eventHandlers;
        for (final String servicePath : servicePathList) {
            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, servicePath, true);
            try {
                pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    String path = event.getData() != null ? event.getData().getPath() : null;
                    PathChildrenCacheEvent.Type type = event.getType();
                    logger.info("事件类型：" + type + "；操作节点：" + path);
                    if (type == PathChildrenCacheEvent.Type.CHILD_ADDED || type == PathChildrenCacheEvent.Type.CHILD_REMOVED || type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                        ServerInfo eventServerInfo = getServerInfo(event.getData().getData());
                        String childNodeName = path.split("/")[path.split("/").length - 1];
                        if (type == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                            if (activeServerMap.get(servicePath) == null) {
                                ConcurrentHashMap<String, ServerInfo> newService = new ConcurrentHashMap<String, ServerInfo>(10);
                                activeServerMap.put(servicePath, newService);
                            }
                            //只有在通过验证时,服务才会加入活跃列表,并触发上线事件.
                            if (serverVerifyHandler == null || serverVerifyHandler.verify(eventServerInfo)) {
                                activeServerMap.get(servicePath).put(childNodeName, eventServerInfo);
                                if (logger.isDebugEnabled()) {
                                    logger.debug(MessageFormat.format("新服务器上线,NODE:{0},info:{1}", childNodeName, JSON.toJSONString(eventServerInfo)));
                                }
                                if (watchers != null) {
                                    for (ServiceEventWatcher watcher : watchers) {
                                        ArrayList<ServerInfo> serverInfos = toArrayList();
                                        watcher.online(new ServiceWatchInvocation(servicePath,childNodeName, serverInfos), eventServerInfo);
                                    }
                                }
                            } else {
                                logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverInfo:{1}", servicePath + "/" + childNodeName, JSON.toJSONString(eventServerInfo)));
                            }
                            //添加节点
                        } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                            ServerInfo serverInfo = activeServerMap.get(servicePath).get(childNodeName);
                            if (logger.isDebugEnabled()) {
                                logger.info(MessageFormat.format("服务器离线,NODE:{0},info:{1}", childNodeName, JSON.toJSONString(serverInfo)));
                            }
                            activeServerMap.get(servicePath).remove(childNodeName);
                            if (watchers != null) {
                                for (ServiceEventWatcher watcher : watchers) {
                                    ArrayList<ServerInfo> serverInfos = toArrayList();
                                    watcher.offline(new ServiceWatchInvocation(servicePath,childNodeName, serverInfos), serverInfo);
                                }
                            }
                            //删除节点
                        } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                            //更新节点
                            ServerInfo oldServerInfo = activeServerMap.get(servicePath).get(childNodeName);
                            if (logger.isDebugEnabled()) {
                                logger.debug(MessageFormat.format("服务器更新,NODE:{0},oldInfo:{1},newInfo:{2}", childNodeName, JSON.toJSONString(oldServerInfo), JSON.toJSONString(eventServerInfo)));

                            }
                            //如果更新之后,服务器未通过验证,那么新的服务不会被添加到活跃列表,旧的服务依然会被移除,并触发离线事件.
                            if (serverVerifyHandler == null || serverVerifyHandler.verify(eventServerInfo)) {
                                activeServerMap.get(servicePath).put(childNodeName, eventServerInfo);
                                if (watchers != null) {
                                    for (ServiceEventWatcher watcher : watchers) {
                                        ArrayList<ServerInfo> serverInfos = toArrayList();
                                        watcher.update(new ServiceWatchInvocation(servicePath,childNodeName, serverInfos), oldServerInfo, eventServerInfo);
                                    }
                                }
                            } else {
                                logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverInfo:{1}", servicePath + "/" + childNodeName, JSON.toJSONString(eventServerInfo)));
                            }
                        }
                    }
                }

                private ArrayList<ServerInfo> toArrayList() {
                    Collection<ServerInfo> values = activeServerMap.get(servicePath).values();
                    ArrayList<ServerInfo> serverInfos = new ArrayList<ServerInfo>();
                    serverInfos.addAll(values);
                    return serverInfos;
                }

            });
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
        Map<String, ConcurrentHashMap<String, ServerInfo>> result = new HashMap<String, ConcurrentHashMap<String, ServerInfo>>(10);
        for (String servicePath : servicePathList) {
            List<String> childs = retryGetChildren(servicePath);
            ConcurrentHashMap<String, ServerInfo> map = new ConcurrentHashMap<String, ServerInfo>(10);
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
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
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
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
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
        if (activeServerMap.get(servicePath) == null) {
            return false;
        }
        return !activeServerMap.get(servicePath).isEmpty();
    }

    /**
     * 强制更活跃服务器列表
     */
    public void refreshActiveServers() {
        this.activeServerMap = initServerMap();
    }
    private ServerInfo getServerInfo(byte[] data) {
        ServerInfo serverInfo = null;
        try {
            serverInfo = JSON.parseObject(new String(data, "utf-8"), ServerInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return serverInfo;
    }

    private ServerInfo getServerInfo(String servicePath) {
        byte[] bytes = retryGetDate(servicePath);
        ServerInfo serverInfo = null;
        try {
            serverInfo = JSON.parseObject(new String(bytes, "utf-8"), ServerInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return serverInfo;
    }

    public void setServerVerifyHandler(ServerVerifyHandler verifyHandler) {
        serverVerifyHandler = verifyHandler;
    }

    public ServerVerifyHandler getServerVerifyHandler() {
        return serverVerifyHandler;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }
}
