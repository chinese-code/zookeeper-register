package test;

import com.alibaba.fastjson.JSON;
import com.github.client.ClientConfiguration;
import com.github.client.ClientManager;
import com.github.client.HostManager;
import com.github.client.ServerEventWatcher;
import com.github.server.ServerInfo;
import org.junit.Test;

/**
 * Created by tumingjian on 2017/3/11.
 */
public class ClientManagerTest3 {
    String connectString="172.18.0.221:2181,172.18.0.222:2181,172.18.0.223:2181";
    String nameSpace="test";
    String serviceName="zufangdai_pc";
    @Test
    public void test3()throws Exception{
        /**
         * 测试类,监视服务的上线下线或更新的事件处理,测试活跃主机列表是否产生相应的变化
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                ClientConfiguration config = new ClientConfiguration(connectString, 30000, 2000, nameSpace, serviceName);
                ClientManager client = new ClientManager(config);
                client.addWatcher(new ServerEventWatcher() {
                    @Override
                    public void online(HostManager manager, ServerInfo currentOnlineServer) {
                        System.out.println(JSON.toJSONString(currentOnlineServer));
                        System.out.println(JSON.toJSONString(manager.getActiveServerInfoList()));
                    }

                    @Override
                    public void offline(HostManager manager, ServerInfo currentOfflineServer) {
                        System.out.println(JSON.toJSONString(currentOfflineServer));
                        System.out.println(JSON.toJSONString(manager.getActiveServerInfoList()));
                    }

                    @Override
                    public void update(HostManager manager, ServerInfo oldServerConfig, ServerInfo newServerConfig) {
                        System.out.println(JSON.toJSONString(newServerConfig));
                        System.out.println(JSON.toJSONString(manager.getActiveServerInfoList()));
                    }
                });
            }
        }).start();
        while (true){
            Thread.sleep(2000);
        }
    }
}
