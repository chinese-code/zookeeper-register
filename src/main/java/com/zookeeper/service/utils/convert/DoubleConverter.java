package com.zookeeper.service.utils.convert;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class DoubleConverter implements Converter<Double> {
    @Override
    public Double convert(Object object) {

        return Double.valueOf(object.toString());
    }
}
