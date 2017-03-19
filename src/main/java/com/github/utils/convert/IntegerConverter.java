package com.github.utils.convert;

/**
 * Created by tumingjian on 2017/3/19.
 */
public class IntegerConverter implements Converter<Integer> {
    public Integer convert(Object object) {
        return Integer.valueOf(object.toString());
    }
}
