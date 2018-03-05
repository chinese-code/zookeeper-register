package com.zookeeper.service.utils.convert;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class BooleanConverter implements Converter<Boolean> {
    @Override
    public Boolean convert(Object object) {
        return Boolean.valueOf(object.toString());
    }
}
