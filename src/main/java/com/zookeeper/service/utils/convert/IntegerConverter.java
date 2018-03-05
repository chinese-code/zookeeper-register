package com.zookeeper.service.utils.convert;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class IntegerConverter implements Converter<Integer> {
    @Override
    public Integer convert(Object object) {
        return Integer.valueOf(object.toString());
    }
}
