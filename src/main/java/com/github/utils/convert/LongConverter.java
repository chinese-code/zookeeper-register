package com.github.utils.convert;

/**
 * Created by tumingjian on 2017/3/19.
 */
public class LongConverter implements Converter<Long> {
    public Long convert(Object object) {
        return Long.valueOf(object.toString());
    }
}
