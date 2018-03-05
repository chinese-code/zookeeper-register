package com.zookeeper.service.utils.convert;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class StringConverter implements  Converter<String> {
    @Override
    public String convert(Object object) {
        return object.toString();
    }
}
