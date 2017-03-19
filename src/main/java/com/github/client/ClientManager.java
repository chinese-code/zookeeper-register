package com.github.client;

import com.alibaba.fastjson.JSON;
import com.github.utils.JSONUtil;
import com.github.server.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import com.github.server.ServerManager;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by tumingjian on 2017/3/11.
 */
public class ClientManager implements HostManager {
    private static Logger logger = Logger.getLogger(ServerManager.class);
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
    private List<ServerEventWatcher> eventHandlers = new ArrayList<ServerEventWatcher>();
    /**
     * 服务器验证处理者.
     */
    private ServerVerifyHandler serverVerifyHandler;

    public ClientManager(ClientConfiguration config) {
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
        this.watche();
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
    protected void watche() {
        activeServerMap = initServerMap();
        InnerCuratorWatcher innerCuratorWatcher = new InnerCuratorWatcher(this);
        watchNode(config.getServiceName(), client, innerCuratorWatcher);
    }

    /**
     * 向列表中添加一个服务事件通知处理器
     *
     * @param watcher
     */
    public void addWatcher(ServerEventWatcher watcher) {
        eventHandlers.add(watcher);
    }

    /**
     * 向列表中移除一个服务事件通知处理器
     *
     * @param watcher
     */
    public void removeWatcher(ServerEventWatcher watcher) {
        eventHandlers.remove(watcher);
    }

    /**
     * 初始化活跃主机列表
     *
     * @return
     */
    private Map<String, ServerInfo> initServerMap() {
        HashMap<String, ServerInfo> map = new HashMap<String, ServerInfo>();
        List<String> childs = retryGetChildren("/" + config.getServiceName());
        for (String child : childs) {
            String path = "/" + config.getServiceName() + "/" + child;
            try {
                ServerInfo serverInfo = getServerInfo(path);
                if (serverInfo != null) {
                    if (this.serverVerifyHandler == null || this.serverVerifyHandler.verify(serverInfo)) {
                        ServerInfo value = new ServerInfo(path, child, config.getNamespace(), config.getServiceName(), serverInfo.getServiceConfig());
                        map.put(child, value);
                    } else {
                        logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverconfig:{1}", path, JSON.toJSONString(serverInfo)));
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
     * @param serviceName
     * @param client
     * @param curatorWatcher
     */
    private void watchNode(String serviceName, CuratorFramework client, CuratorWatcher curatorWatcher) {
        try {
            List<String> childs = client.getChildren().usingWatcher(curatorWatcher).forPath("/" + serviceName);
            for (String childNode : childs) {
                String path = "/" + serviceName + "/" + childNode;
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

    public Collection<ServerInfo> getActiveServerInfoList() {
        return activeServerMap.values();
    }

    /**
     * 强制更活跃服务器列表
     */
    public void refreshActiveServerInfoList() {
        this.activeServerMap = initServerMap();
    }


    private ServerInfo getServerInfo(String path) {
        byte[] bytes = retryGetDate(path);
        return JSONUtil.parse(new String(bytes), ServerInfo.class);
    }

    public void setServerVerifyHandler(ServerVerifyHandler verifyHandler) {
        serverVerifyHandler=verifyHandler;
    }

    public ServerVerifyHandler getServerVerifyHandler() {
        return serverVerifyHandler;
    }

    /**
     * Zookeeper Watcher的实现类,实现了一个Watcher事件转换为服务器上线,下线和服务器配置更新事件触发时通知
     * 到多个ServerEventWatcher事件上.
     */
    private class InnerCuratorWatcher implements CuratorWatcher {
        final private String serviceName;
        final private List<ServerEventWatcher> handlers;
        final private ClientManager manager;

        public InnerCuratorWatcher(ClientManager manager) {
            this.manager = manager;
            this.serviceName = config.getServiceName();
            this.handlers = manager.eventHandlers;

        }

        /**
         * zookeeper监听事件处理.
         * @param event
         * @throws Exception
         */
        @Override
        public void process(WatchedEvent event) throws Exception {
            if (event.getType() == Watcher.Event.EventType.None) {
                logger.info("Service监视器注册失败!");
            } else {
                String path = event.getPath().replace("/", "");
                logger.info("watchedEvent:" + event);
                //server offline
                if (event.getType() == Watcher.Event.EventType.NodeDeleted && !path.equals(serviceName)) {
                    String child = path.substring(serviceName.length());
                    ServerInfo serverInfo = activeServerMap.get(child);
                    logger.info(MessageFormat.format("服务器离线,NODE:{0},info:{1}", child, JSON.toJSONString(serverInfo)));
                    activeServerMap.remove(child);
                    if (handlers != null) {
                        for (ServerEventWatcher watcher : handlers) {
                            watcher.offline(manager, serverInfo);
                        }
                    }
                    //server update
                } else if (event.getType() == Watcher.Event.EventType.NodeDataChanged && !path.equals(serviceName)) {
                    String child = path.substring(serviceName.length());
                    ServerInfo oldServerInfo = activeServerMap.get(child);
                    ServerInfo newServerInfo = getServerInfo(event.getPath());
                    newServerInfo = new ServerInfo(event.getPath(), child, config.getNamespace(), config.getServiceName(), newServerInfo.getServiceConfig());
                    //如果更新之后,服务器未通过验证,那么新的服务不会被添加到活跃列表,旧的服务依然会被移除,并触发离线事件.
                    if (manager.serverVerifyHandler == null || manager.serverVerifyHandler.verify(newServerInfo)) {
                        activeServerMap.put(child, newServerInfo);
                        logger.info(MessageFormat.format("服务器更新,NODE:{0},oldInfo:{1},newInfo:{2}", child, JSON.toJSONString(oldServerInfo), JSON.toJSONString(newServerInfo)));
                        if (handlers != null) {
                            for (ServerEventWatcher watcher : handlers) {
                                watcher.update(manager, oldServerInfo, newServerInfo);
                            }
                        }
                    } else {
                        logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverconfig:{1}", event.getPath(), JSON.toJSONString(newServerInfo)));
                        activeServerMap.remove(child);
                        logger.info(MessageFormat.format("服务器离线,NODE:{0},info:{1}", child, JSON.toJSONString(newServerInfo)));
                        if (handlers != null) {
                            for (ServerEventWatcher watcher : handlers) {
                                watcher.offline(manager, newServerInfo);
                            }
                        }
                    }
                    //server online
                } else if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged && path.equals(serviceName)) {
                    List<String> childrens = retryGetChildren(event.getPath());
                    for (String child : childrens) {
                        ServerInfo serverInfo = getServerInfo("/" + serviceName + "/" + child);
                        ServerInfo value = new ServerInfo("/" + serviceName + "/" + child, child, config.getNamespace(), config.getServiceName(), serverInfo.getServiceConfig());
                        if (activeServerMap.get(child) == null) {
                            //只有在通过验证时,服务才会加入活跃列表,并触发上线事件.
                            if (manager.serverVerifyHandler == null || manager.serverVerifyHandler.verify(value)) {
                                activeServerMap.put(child, value);
                                logger.info(MessageFormat.format("新服务器上线,NODE:{0},info:{1}", child, JSON.toJSONString(value)));
                                if (handlers != null) {
                                    for (ServerEventWatcher watcher : handlers) {
                                        watcher.online(manager, value);
                                    }
                                }
                            } else {
                                logger.error(MessageFormat.format("服务器未通过验证,path:{0},serverconfig:{1}", "/" + serviceName + "/" + child, JSON.toJSONString(value)));
                            }
                        }
                    }
                }
            }
            /**
             *由于zookeeper Watcher一次注册只能一次监听,所以需要再次注册新的监听器
             */
            watchNode(serviceName, client, this);
        }
    }

    public ClientConfiguration getConfig() {
        return config;
    }

    public List<ServerEventWatcher> getEventHandlers() {
        return eventHandlers;
    }
}
