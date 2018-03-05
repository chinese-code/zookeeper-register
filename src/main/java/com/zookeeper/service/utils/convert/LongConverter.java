package com.zookeeper.service.utils.convert;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class LongConverter implements Converter<Long> {
    @Override
    public Long convert(Object object) {
        return Long.valueOf(object.toString());
    }
}
