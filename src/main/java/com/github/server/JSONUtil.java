package com.github.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * Created by tumingjian on 2017/3/12.
 */
public class JSONUtil {
    /**
     * JSON反射对象,对于没有SET方法的对象使用.但目前基本类型只支持String.如果对象中有List<Object>
     *     或Map<String,Object>这样的属性类型,Object将转换为JSONObject
     * @param json  json字符串
     * @param tClass  要转换的目标对象CLASS
     * @return 返回被成功转换的对象
     * @throws Exception
     */
    public static <T> T parse(String json,Class<T> tClass){
        T t =null;
        try{
            t = tClass.newInstance();
            JSONObject jsonObject = JSON.parseObject(json);
            Field[] fields = ServerInfo.class.getDeclaredFields();
            for(Field field:fields){
                Object o = jsonObject.get(field.getName());
                field.setAccessible(true);
                if(o!=null){
                    Object value =o;
                    try{
                        Class<?> type = field.getType();
                        if(!isBaseType(type)){
                            value=JSON.parseObject(o.toString(), type);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    field.set(t,value);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return t;
    }

    public static boolean isBaseType(Class<?> type){
        boolean isBaseType=type==String.class || type==Boolean.class || type==boolean.class ||
                type==Integer.class || type==int.class || type==Long.class || type==long.class
                || type==Short.class || type==short.class || type==Character.class || type==char.class
                || type==Double.class || type==double.class || type== BigDecimal.class;
        return isBaseType;
    }
}
