package test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.server.JSONUtil;
import com.github.server.ServerInfo;
import com.github.service.ServiceConfiguration;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Created by tumingjian on 2017/3/12.
 */
public class TestJsonConvert {
    @Test
    public void test1()throws Exception{
        ServerInfo serverInfo = new ServerInfo(null, null, null, null, null);
        String bytes="{\"namespace\":\"test\",\"serviceConfig\":{\"port\":\"8080\",\"ip\":\"2.2.2.2\",\"onlineTime\":\"1489311186969\"},\"serviceName\":\"zufangdai_pc\"}\n";
        ServerInfo parse = JSONUtil.parse(bytes, ServerInfo.class);
        System.out.println(parse);
    }
}