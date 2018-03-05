package com.github;

import com.github.server.ServiceConfig;
import com.github.server.ServiceRegister;
import com.github.service.ServiceNodeData;
import com.github.zookeeper.ZookeeperConfiguration;
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
    //zookeeper 服务器列表
    String connectString = "127.0.0.1:2181";
    //curator-framework zookepper 命名空间
    String namespace = "test";
    //company
    String companyName = "com.zhongjinlianhe";
    //serviceLine
    String serviceLine = "base";
    int connectionTimeout = 30000;
    int sessionTimeout = 2000;
    public void register(String serviceName,String ip, String port) {
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                connectionTimeout, sessionTimeout, namespace);
        ServiceNodeData serverData = new ServiceNodeData(ip, port);
        ServiceConfig serverInfo = new ServiceConfig(companyName, serviceLine, serviceName);
        //创建一个注册服务管理对象
        ServiceRegister server = new ServiceRegister(zookeeperConfiguration, serverInfo, serverData);
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
        register("Service.SnowFlakeGenId","192.168.1.5","1000");
    }

    @Test
    public void simpleServer2() throws Exception {
        register("Service.SnowFlakeGenId","192.168.1.4","1000");
    }
    @Test
    public void simpleServer3() throws Exception {
        register("Service.UserCenter","192.168.1.4","1000");
    }
    @Test
    public void simpleServer4() throws Exception {
        register("Service.UserCenter","192.168.1.5","1000");
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
