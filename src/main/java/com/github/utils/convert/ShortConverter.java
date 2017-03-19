package com.github.utils.convert;

/**
 * Created by tumingjian on 2017/3/19.
 */
public class ShortConverter implements Converter<Short> {
    public Short convert(Object object) {
        return Short.valueOf(object.toString());
    }
}
