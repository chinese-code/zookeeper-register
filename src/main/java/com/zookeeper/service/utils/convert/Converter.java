package com.zookeeper.service.utils.convert;

/**
 * @author tumingjian
 * @date 2018/3/5
 * 说明:
 */
public interface Converter<T> {
    /**
     * 转换器,将一个类型的值转换为另一个类型的值
     * @param object
     * @return
     */
    T convert(Object object);
}
