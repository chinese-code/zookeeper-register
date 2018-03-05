package com.zookeeper.service.utils.convert;

import java.math.BigDecimal;

/**
 * @author tumingjian
 * @date 2017/3/19.
 */
public class BigDecimalConverter implements Converter<BigDecimal> {
    @Override
    public BigDecimal convert(Object object) {
        return new BigDecimal(object.toString());
    }
}
