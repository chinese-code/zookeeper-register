package com.github.utils.convert;

/**
 * Created by tumingjian on 2017/3/19.
 */
public interface Converter<T> {
    T convert(Object object);
}
