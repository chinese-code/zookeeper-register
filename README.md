# zookeeper-register
基于zookeeper 一个能将服务注册,监听,获取所有注册主机信息简单化的项目,你只需简单的几行代码,便可以完成服务的上线,下线,监听功能

# zookeeper-register-demo

https://github.com/tumingjian/zookeeper-register/blob/master/src/main/test/test/ZookeeperRegisterDemo.java


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

