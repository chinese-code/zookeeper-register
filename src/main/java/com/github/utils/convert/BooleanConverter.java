package com.github.utils.convert;

/**
 * Created by tumingjian on 2017/3/19.
 */
public class BooleanConverter implements Converter<Boolean> {
    public Boolean convert(Object object) {
        return Boolean.valueOf(object.toString());
    }
}
