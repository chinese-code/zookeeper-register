# zookeeper-register
基于zookeeper 一个能将服务注册,监听,获取所有注册主机信息简单化的项目,你只需简单的几行代码,便可以完成服务的上线,下线,监听功能

# zookeeper-register-demo

https://github.com/tumingjian/zookeeper-register/blob/master/src/test/java/com/zookeeper/service/ZookeeperServiceRegisterDemo.java

#ZookeeperServiceRegisterDemo

 package com.zookeeper.service;

 import com.zookeeper.service.server.ServiceConfig;
 import com.zookeeper.service.server.ServiceRegister;
 import com.zookeeper.service.server.ServiceNodeData;
 import com.zookeeper.service.zookeeper.ZookeeperConfiguration;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 /**
  * @author tumingjian
  * @date 2018/3/5
  * 说明:
  */
 public class ZookeeperServiceRegisterDemo {

     private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceRegisterDemo.class);
     //zookeeper 服务器列表,多个用逗号隔开
     String connectString = "127.0.0.1:2181";
     //curator-framework zookepper 命名空间
     String namespace = "test";
     //公司名
     String companyName = "com.zhongjinlianhe";
     //业务线
     String serviceLine = "base";
     int connectionTimeout = 30000;
     int sessionTimeout = 2000;

     /**
      * 注册服务到zookeeper中.
      * @param serviceName
      * @param ip
      * @param port
      */
     public void register(String serviceName,String ip, String port) {
         ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                 connectionTimeout, sessionTimeout, namespace);
         ServiceNodeData serverData = new ServiceNodeData(ip, port);
         ServiceConfig serviceConfig = new ServiceConfig(companyName, serviceLine, serviceName);
         //创建一个注册服务管理对象
         ServiceRegister server = new ServiceRegister(zookeeperConfiguration, serviceConfig, serverData);
         //注册到zookeeper
         server.online();
     }
     /**
      * 注册一个服务到zookeeper中
      *
      * @throws Exception
      */

     @Test
     public void simpleServer1() throws Exception {
         register("Service.SnowFlakeGenId","192.168.1.2","1000");
     }

     @Test
     public void simpleServer2() throws Exception {
         register("Service.SnowFlakeGenId","192.168.1.3","1000");
     }
     @Test
     public void simpleServer3() throws Exception {
         register("Service.UserCenter","192.168.1.4","2000");
     }
     @Test
     public void simpleServer4() throws Exception {
         register("Service.UserCenter","192.168.1.5","2000");
     }

     @Test
     public void test1()throws Exception{
         simpleServer1();
         simpleServer2();
         simpleServer3();
         simpleServer4();
         while (true){
             Thread.sleep(1000);
         }
     }
 }


# zookeeper-register-demo

https://github.com/tumingjian/zookeeper-register/blob/master/src/test/java/com/zookeeper/service/ZookeeperServiceClientDemo.java

#ZookeeperServiceClientDemo

package com.zookeeper.service;

import com.alibaba.fastjson.JSON;
import com.zookeeper.service.client.*;
import com.zookeeper.service.zookeeper.ZookeeperConfiguration;
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
    //zookeeper 服务器列表,多个用逗号隔开
    String connectString = "127.0.0.1:2181";
    //要连接的服务列表字符串,多个服务字符串用逗号隔开
    private String servicePathList = "/com.zhongjinlianhe/base/Service.SnowFlakeGenId,/com.zhongjinlianhe/base/Service.UserCenter";
    //服务1
    private String snowFlakeGenIdServicePath = "/com.zhongjinlianhe/base/Service.SnowFlakeGenId";
    //服务2
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
        ClientConfig config = new ClientConfig(zookeeperConfiguration, servicePathList);
        ServiceClient client = new ServiceClient(config);
        //监听服务器状态,一次就可以,不用重复注册
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
        Collection<ServerInfo> list = client.getActiveServers(snowFlakeGenIdServicePath);
        logger.info("当前活跃主机:" + JSON.toJSONString(list));
        //获取已注册的服务器列表
        Collection<ServerInfo> list2 = client.getActiveServers(userCenterServicePath);
        logger.info("当前活跃主机:" + JSON.toJSONString(list2));

        while (true) {
            Thread.sleep(1000);
        }
    }
}
