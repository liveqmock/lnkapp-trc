package org.fbi.linking.codec.dataformat.format;

import org.fbi.linking.codec.dataformat.Format;

public class StringFormat implements Format<String> {

    public String format(String object) throws Exception {
        return object;
    }

    public String parse(String string) throws Exception {
        return string;
    }

}
