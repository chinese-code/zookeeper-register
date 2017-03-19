package com.github.utils.convert;

/**
 * Created by tumingjian on 2017/3/19.
 */
public class DoubleConverter implements Converter<Double> {
    public Double convert(Object object) {

        return Double.valueOf(object.toString());
    }
}
