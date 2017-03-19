package com.github.utils.convert;

/**
 * Created by tumingjian on 2017/3/19.
 */
public class StringConverter implements  Converter<String> {
    public String convert(Object object) {
        return object.toString();
    }
}
