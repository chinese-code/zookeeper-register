package com.github;

import com.github.server.ServiceConfig;
import org.junit.Test;
import com.github.service.ServiceNodeData;
import com.github.server.ServiceRegister;
import com.github.zookeeper.ZookeeperConfiguration;

/**
 * Created by tumingjian on 2017/3/11.
 */
public class ServiceManagerTest2 {
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
    @Test
    public void test2()throws Exception{
        /**
         * 测试类,服务器先上线,再更新服务器信息
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 测试类,服务器先上线再离线.
                 */

                //zookeeper config
                ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                        connectionTimeout, sessionTimeout, namespace);
                //service config
                ServiceNodeData serverData = new ServiceNodeData("192.168.1.3", "10080");
                ServiceConfig serverInfo=new ServiceConfig(companyName,serviceLine,serviceName);
                //创建一个注册服务管理对象
                ServiceRegister server = new ServiceRegister(zookeeperConfiguration,serverInfo,serverData);
                //注册到zookeeper
                server.online();
                try{
                    Thread.sleep(1000);
                }catch (Exception e){

                }
                serverData.setHost("4.4.4.4");
                serverData.setPort("9090");
                serverData.put("memo","更新服务器信息.");
                ZookeeperConfiguration a = new ZookeeperConfiguration("1", 2, 3, "a");
                serverData.put("zookeeperConfig",a);
                server.updateCurrent(serverData);
            }
        }).start();
        while (true){
            Thread.sleep(5000);
        }
    }
}
