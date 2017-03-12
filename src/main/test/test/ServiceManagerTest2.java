package test;

import org.junit.Test;
import com.github.service.ServiceConfiguration;
import com.github.server.ServerManagerConfig;
import com.github.server.ServerManagerConfiguration;
import com.github.server.ServerManager;
import com.github.zookeeper.ZookeeperConfiguration;

/**
 * Created by tumingjian on 2017/3/11.
 */
public class ServiceManagerTest2 {
    String connectString="172.18.0.221:2181,172.18.0.222:2181,172.18.0.223:2181";
    String nameSpace="test";
    String serviceName="zufangdai_pc";
    @Test
    public void test2()throws Exception{
        /**
         * 测试类,服务器先上线,再更新服务器信息
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(connectString, 30000, 2000, nameSpace);
                ServiceConfiguration serviceConfiguration = new ServiceConfiguration("2.2.2.2", "8080");
                serviceConfiguration.put("onlineTime",String.valueOf(System.currentTimeMillis()));
                ServerManagerConfig config = new ServerManagerConfiguration(zookeeperConfiguration, serviceName,serviceConfiguration);
                ServerManager service = new ServerManager(config);
                service.online();
                try{
                    Thread.sleep(1000);
                }catch (Exception e){

                }
                serviceConfiguration.setHost("4.4.4.4");
                serviceConfiguration.setPort("9090");
                serviceConfiguration.put("memo","更新服务器信息.");
                ZookeeperConfiguration a = new ZookeeperConfiguration("1", 2, 3, "a");
                serviceConfiguration.put("zookeeperConfig",a);
                service.updateCurrent(serviceConfiguration);
            }
        }).start();
        while (true){
            Thread.sleep(5000);
        }
    }
}
