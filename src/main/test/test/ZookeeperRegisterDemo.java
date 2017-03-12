package test;

import com.alibaba.fastjson.JSONObject;
import com.github.client.*;
import com.github.server.ServerInfo;
import com.github.server.ServerManager;
import com.github.server.ServerManagerConfiguration;
import com.github.service.ServiceConfiguration;
import com.github.zookeeper.ZookeeperConfiguration;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by tumingjian on 2017/3/12.
 */
public class ZookeeperRegisterDemo {
    //zookeeper 服务器列表
    String connectString="172.18.0.221:2181,172.18.0.222:2181,172.18.0.223:2181";
    //curator-framework zookepper 命名空间
    String namespace="test_environment";
    //服务名,同一个服务名下可以注册多个节点
    String serviceName="user_register_service_pc";
    int connectionTimeout=30000;
    int sessionTimeout=2000;

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
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration("192.168.1.3", "10080");
        //server config
        ServerManagerConfiguration serverManagerConfiguration = new ServerManagerConfiguration(
                zookeeperConfiguration, serviceName, serviceConfiguration);
        //创建一个注册服务管理对象
        ServerManager server = new ServerManager(serverManagerConfiguration);
        //注册到zookeeper
        server.online();
    }

    /**
     * 创建一个服务的client,并获取这个服务的所有可用主机列表
     * @throws Exception
     */
    @Test
    public void simpleClient()throws Exception{
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString,
                connectionTimeout, sessionTimeout, namespace);
        ClientConfiguration config = new ClientConfiguration(zookeeperConfiguration, serviceName);
        ClientManager client = new ClientManager(config);
        //获取已注册的服务器列表
        Collection<ServerInfo> list = client.getActiveServerInfoList();
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
        //service config
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration("192.168.1.3", "10080");
        serviceConfiguration.put("memo","PC端用户注册服务器");
        //设置一个口令,让客户端可以通过该口令来验证服务器的合法性.
        serviceConfiguration.put("verifyCode","一支穿云剑,千军万马来相见");
        //server config
        ServerManagerConfiguration serverManagerConfiguration = new ServerManagerConfiguration(
                zookeeperConfiguration, serviceName, serviceConfiguration);
        ServerManager server = new ServerManager(serverManagerConfiguration);
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
        ClientManager client = new ClientManager(config);
        //设置一个用于验证服务器是否合法的验证处理器
        client.setServerVerifyHandler(new ServerVerifyHandler() {
            @Override
            public boolean verify(ServerInfo serverInfo) {
                JSONObject verifyCode = (JSONObject)serverInfo.getServiceConfig().get("verifyCode");
                if(verifyCode.toString().equals("一支穿云箭,千军万马来相见")){
                    return true;
                }else{
                    return false;
                }
            }
        });
        //添加一个服务器上线或下线的通知处理器
        client.addWatcher(new ServerEventWatcher() {
            @Override
            public void online(HostManager manager, ServerInfo currentOnlineServer) {
                System.out.println("有服务器上线啦");
            }

            @Override
            public void offline(HostManager manager, ServerInfo currentOfflineServer) {
                System.out.println("有服务器下线啦");
            }

            @Override
            public void update(HostManager manager, ServerInfo oldServerConfig, ServerInfo newServerConfig) {
                System.out.println("有服务器配置更新啦");
            }
        });
    }
}
