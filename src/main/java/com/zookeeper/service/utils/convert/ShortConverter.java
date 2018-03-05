package com.zookeeper.service.utils.convert;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class ShortConverter implements Converter<Short> {
    @Override
    public Short convert(Object object) {
        return Short.valueOf(object.toString());
    }
}
