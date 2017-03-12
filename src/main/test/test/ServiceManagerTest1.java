package test;

import org.junit.Test;
import com.github.server.ServerManagerConfig;
import com.github.server.ServerManagerConfiguration;
import com.github.server.ServerManager;

/**
 * Created by tumingjian on 2017/3/11.
 */
public class ServiceManagerTest1 {
    String connectString="172.18.0.221:2181,172.18.0.222:2181,172.18.0.223:2181";
    String nameSpace="test";
    String serviceName="zufangdai_pc";
    @Test
    public void test1()throws Exception{
        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 测试类,服务器先上线再离线.
                 */
                ServerManagerConfig config = new ServerManagerConfiguration(connectString, 30000, 2000, nameSpace, serviceName, "1.1.1.1", "8080");
                ServerManager service = new ServerManager(config);
                service.online();
                try{
                    Thread.sleep(1000);
                    service.offline();
                    System.out.println(service.isOffline());
                    Thread.sleep(1000);
                    service.online();
                }catch (Exception e){

                }
            }
        }).start();
        while (true){
            Thread.sleep(5000);
        }
    }
}
