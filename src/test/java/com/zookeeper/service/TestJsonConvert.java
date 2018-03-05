package com.zookeeper.service;

import com.zookeeper.service.server.ServiceConfig;
import com.zookeeper.service.utils.JsonUtils;
import com.zookeeper.service.utils.convert.Converter;
import org.junit.Test;

import java.util.Map;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class TestJsonConvert {
    @Test
    public void test1()throws Exception{
//        ServiceConfig serverInfo = new ServiceConfig("server", "sss", "ddd");
//        String bytes="{\"namespace\":\"test\",\"serviceConfig\":{\"port\":\"8080\",\"ip\":\"2.2.2.2\",\"onlineTime\":\"1489311186969\"},\"serviceName\":\"zufangdai_pc\"}\n";
//        ServiceConfig parse = JsonUtils.parse(bytes, ServiceConfig.class);
//        System.out.println(parse);
    }

    @Test
    public void test2()throws Exception{
        Map<Class<?>, Converter> map = JsonUtils.converterMap();
        Converter converter = map.get(Integer.class);
        Integer convert = (Integer)converter.convert("34567");
        System.out.println(convert);
    }
}
