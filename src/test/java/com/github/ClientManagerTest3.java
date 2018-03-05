package com.github;

import com.alibaba.fastjson.JSON;
import com.github.client.*;
import org.junit.Test;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ClientManagerTest3 {
    String connectString="172.18.0.221:2181,172.18.0.222:2181,172.18.0.223:2181";
    String nameSpace= "test";
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
                Client client = new Client(config);
                client.addWatcher(new ServiceEventWatcher() {
                    @Override
                    public void online(ServiceWatchInvocation invoker, ServerInfo currentOnlineServer) {
                        System.out.println(JSON.toJSONString(currentOnlineServer));
                        System.out.println(JSON.toJSONString(invoker.getActiveServerList()));
                    }

                    @Override
                    public void offline(ServiceWatchInvocation invoker, ServerInfo currentOfflineServer) {
                        System.out.println(JSON.toJSONString(currentOfflineServer));
                        System.out.println(JSON.toJSONString(invoker.getActiveServerList()));
                    }

                    @Override
                    public void update(ServiceWatchInvocation invoker, ServerInfo oldServerInfo, ServerInfo newServerInfo) {
                        System.out.println(JSON.toJSONString(newServerInfo));
                        System.out.println(JSON.toJSONString(invoker.getActiveServerList()));
                    }
                });
            }
        }).start();
        while (true){
            Thread.sleep(2000);
        }
    }
}
