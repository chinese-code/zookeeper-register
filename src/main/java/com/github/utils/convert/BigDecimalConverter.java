package com.github.utils.convert;

import java.math.BigDecimal;

/**
 * Created by tumingjian on 2017/3/19.
 */
public class BigDecimalConverter implements Converter<BigDecimal> {
    public BigDecimal convert(Object object) {
        return new BigDecimal(object.toString());
    }
}
