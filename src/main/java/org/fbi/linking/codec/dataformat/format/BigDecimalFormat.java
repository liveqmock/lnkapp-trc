package org.fbi.linking.codec.dataformat.format;


import org.fbi.linking.codec.dataformat.Format;

import java.math.BigDecimal;

public class BigDecimalFormat implements Format<BigDecimal> {

    private int precision = -1;

    public BigDecimalFormat(int precision) {
        this.precision = precision;
    }

    public BigDecimalFormat() {
    }

    public String format(BigDecimal object) throws Exception {
        return object.toString();
    }

    public BigDecimal parse(String string) throws Exception {
        BigDecimal result = new BigDecimal(string);
        if (precision != -1) {
            result = result.setScale(precision);
        }
        return result;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }
}
