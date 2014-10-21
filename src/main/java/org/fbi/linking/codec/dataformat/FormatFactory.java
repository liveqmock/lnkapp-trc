package org.fbi.linking.codec.dataformat;

import org.fbi.linking.codec.dataformat.format.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * User: zhanrui
 * Date: 13-9-7
 */

public final class FormatFactory {

    private FormatFactory() {
    }

    public static Format<?> getFormat(Class<?> clazz, String pattern, int precision) throws Exception {
        if  (clazz == short.class || clazz == Short.class) {
            return pattern != null ? new ShortPatternFormat(pattern) : new ShortFormat();

        } else if (clazz == BigDecimal.class) {
            return new BigDecimalFormat(precision);

        } else if (clazz == String.class) {
            return new StringFormat();

        } else if (clazz == Date.class) {
            return new DatePatternFormat(pattern);

        } else {
            throw new IllegalArgumentException("Formatter err for the type: " + clazz.getCanonicalName());
        }
    }

}
