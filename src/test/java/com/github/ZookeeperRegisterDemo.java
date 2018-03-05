package com.github;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.client.*;
import com.github.server.ServiceConfig;
import com.github.server.ServiceRegister;
import com.github.service.ServiceNodeData;
import com.github.zookeeper.ZookeeperConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by tumingjian on 2017/3/12.
 */
public class ZookeeperRegisterDemo {
    private static final Logger logger= LoggerFactory.getLogger(ZookeeperRegisterDemo.class);
    //zookeeper 服务器列表
    String connectString="127.0.0.1:2181";
    //curator-framework zookepper 命名空间
    String namespace= "test";
    //company
    String companyName="com.zhongjinlianhe";
    //serviceLine
    String serviceLine="base";
    //服务名,同一个服务名下可以注册多个节点
    String serviceName="Service.SnowFlakeGenId";
    int connectionTimeout=30000;
    int sessionTimeout=2000;
    private String servicePath="/com.zhongjinlianhe/base/Service.SnowFlakeGenId";
    /**
     * 注册一个服务到zookeeper中
     * @throws Exception
     */
    @Test
    public void simpleServer()throws Exception{
        //zookeeper config
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                connectionTimeout, sessionTimeout, namespace);
        //service config
        ServiceNodeData serverData = new ServiceNodeData("192.168.1.4", "10080");
        ServiceConfig serverInfo=new ServiceConfig(companyName,serviceLine,serviceName);
        //创建一个注册服务管理对象
        ServiceRegister server = new ServiceRegister(zookeeperConfiguration,serverInfo,serverData);
        //注册到zookeeper
        server.online();
        while(true){
            Thread.sleep(1000);
        }
    }
    /**
     * 注册一个服务到zookeeper中
     * @throws Exception
     */
    @Test
    public void simpleServer1()throws Exception{
        //zookeeper config
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                connectionTimeout, sessionTimeout, namespace);
        //service config
        ServiceNodeData serverData = new ServiceNodeData("192.168.1.5", "10080");
        ServiceConfig serverInfo=new ServiceConfig(companyName,serviceLine,serviceName);
        //创建一个注册服务管理对象
        ServiceRegister server = new ServiceRegister(zookeeperConfiguration,serverInfo,serverData);
        //注册到zookeeper
        server.online();
        while(true){
            Thread.sleep(1000);
        }
    }
    /**
     * 创建一个服务的client,并获取这个服务的所有可用主机列表
     * @throws Exception
     */
    @Test
    public void simpleClient()throws Exception{
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                connectionTimeout, sessionTimeout, namespace);
        ClientConfiguration config = new ClientConfiguration(zookeeperConfiguration, servicePath);
        ServiceClient client = new ServiceClient(config);
        //获取已注册的服务器列表
        Collection<ServerInfo> list = client.getActiveServers();
        logger.info("当前活跃主机:"+ JSON.toJSONString(list));

        while(true){
            Thread.sleep(1000);
        }
    }

    /**
     * 注册一个服务到zookeeper中,并添加了一些定义的配置信息.
     * @throws Exception
     */
    @Test
    public void serverAndMoreServiceConfig()throws Exception{
        //zookeeper config
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                connectionTimeout, sessionTimeout, namespace);
        //server config
        ServiceConfig serverInfo=new ServiceConfig(companyName,serviceLine,serviceName);
        //service config
        ServiceNodeData serverData = new ServiceNodeData("192.168.1.5", "10080");
        serverData.put("memo","PC端用户注册服务器");
        //设置一个口令,让客户端可以通过该口令来验证服务器的合法性.
        serverData.put("verifyCode","一支穿云剑,千军万马来相见");
        ServiceRegister server = new ServiceRegister(zookeeperConfiguration,serverInfo,serverData);
        //注册到zookeeper
        server.online();
    }

    /**
     * 创建一个服务的client,并监听这些服务器的运行情况.
     * @throws Exception
     */
    @Test
    public void clientAndVerifyAndWatcher()throws Exception{
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                connectionTimeout, sessionTimeout, namespace);
        ClientConfiguration config = new ClientConfiguration(zookeeperConfiguration, serviceName);
        ServiceClient client = new ServiceClient(config);
        //设置一个用于验证服务器是否合法的验证处理器
        client.setServerVerifyHandler(new ServerVerifyHandler() {
            @Override
            public boolean verify(ServiceNodeData serverInfo) {
                String verifyCode=serverInfo.getOrDefault("verifyCode","").toString();
                if(verifyCode.toString().equals("一支穿云箭,千军万马来相见")){
                    return true;
                }else{
                    return false;
                }
            }
        });
        //添加一个服务器上线或下线的通知处理器
        client.addWatcher(new ServiceHostEventWatcher() {
            @Override
            public void online(ActiveServerInfo activeServerInfo, ServerInfo currentOnlineServer) {
                System.out.println("有服务器上线啦");
            }

            @Override
            public void offline(ActiveServerInfo activeServerInfo, ServerInfo currentOfflineServer) {
                System.out.println("有服务器下线啦");
            }

            @Override
            public void update(ActiveServerInfo activeServerInfo, ServerInfo oldServerConfig, ServerInfo newServerConfig) {
                System.out.println("有服务器配置更新啦");
            }
        });
    }
}
