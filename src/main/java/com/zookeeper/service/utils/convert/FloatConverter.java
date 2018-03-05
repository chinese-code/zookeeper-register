package com.zookeeper.service.utils.convert;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public class FloatConverter implements  Converter<Float> {
    @Override
    public Float convert(Object object) {
        return Float.valueOf(object.toString());
    }
}
