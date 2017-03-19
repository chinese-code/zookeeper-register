package com.github.utils.convert;

/**
 * Created by tumingjian on 2017/3/19.
 */
public class FloatConverter implements  Converter<Float> {
    public Float convert(Object object) {
        return Float.valueOf(object.toString());
    }
}
