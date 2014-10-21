package org.fbi.linking.codec.dataformat.format;

import org.fbi.linking.codec.dataformat.PatternFormat;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public abstract class NumberPatternFormat<T> implements PatternFormat<T> {

    private String pattern;

    public NumberPatternFormat() {
    }

    public NumberPatternFormat(String pattern) {
        this.pattern = pattern;
    }

    public String format(T object) throws Exception {
        return this.getNumberFormat().format(object);
    }

    @SuppressWarnings("unchecked")
    public T parse(String string) throws Exception {
        return (T)this.getNumberFormat().parse(string);
    }

    protected NumberFormat getNumberFormat() {
        NumberFormat format = NumberFormat.getNumberInstance();
        if (format instanceof DecimalFormat) {
            ((DecimalFormat)format).applyLocalizedPattern(pattern);
        }
        return format;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
