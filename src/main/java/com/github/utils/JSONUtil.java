package com.github.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.utils.convert.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tumingjian on 2017/3/12.
 */
public class JSONUtil {
    /**
     * JSON反射对象,对于没有SET方法的对象使用.但目前基本类型只支持String.如果对象中有List<Object>
     * 或Map<String,Object>这样的属性类型,Object将转换为JSONObject
     *
     * @param json   json字符串
     * @param tClass 要转换的目标对象CLASS
     * @return 返回被成功转换的对象
     * @throws Exception
     */
    public static <T> T parse(String json, Class<T> tClass) {
        T t = null;
        Map<Class<?>, Converter> map = converterMap();
        try {
            t = tClass.newInstance();
            JSONObject jsonObject = JSON.parseObject(json);
            Field[] fields = tClass.getDeclaredFields();
            for (Field field : fields) {
                Object o = jsonObject.get(field.getName());
                field.setAccessible(true);
                if (o != null) {
                    Object value = o;
                    try {
                        Class<?> type = field.getType();
                        Converter converter = map.get(type);
                        if(converter!=null){
                            value = converter.convert(o);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    field.set(t, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }
    public static Map<Class<?>, Converter> converterMap() {
        HashMap<Class<?>, Converter> result = new HashMap<Class<?>, Converter>();
        result.put(String.class, new StringConverter());

        result.put(int.class, new IntegerConverter());
        result.put(Integer.class, new IntegerConverter());

        result.put(float.class, new FloatConverter());
        result.put(Float.class, new FloatConverter());

        result.put(double.class, new DoubleConverter());
        result.put(Double.class, new DoubleConverter());

        result.put(long.class, new LongConverter());
        result.put(Long.class, new LongConverter());

        result.put(short.class, new ShortConverter());
        result.put(Short.class, new ShortConverter());

        result.put(Boolean.class, new BooleanConverter());
        result.put(boolean.class, new BooleanConverter());

        result.put(BigDecimal.class, new BigDecimalConverter());

        return result;

    }

}
