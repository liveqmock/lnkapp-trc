package org.fbi.linking.codec.dataformat.format;

import org.fbi.linking.codec.dataformat.PatternFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePatternFormat implements PatternFormat<Date> {

    private String pattern;

    public DatePatternFormat() {
    }

    public DatePatternFormat(String pattern) {
        this.pattern = pattern;
    }

    public String format(Date object) throws Exception {
        return this.getDateFormat().format(object);
    }

    public Date parse(String string) throws Exception {

        Date date;
        DateFormat df = this.getDateFormat();

        if (string.length() <= this.pattern.length()) {
            df.setLenient(false);
            date = df.parse(string);

            return date;

        } else {
            throw new RuntimeException("Date Pattern not defined!");
        }

    }

    protected DateFormat getDateFormat() {
        return new SimpleDateFormat(this.pattern);
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
