package org.fbi.linking.codec.dataformat.format;


import org.fbi.linking.codec.dataformat.Format;

public class ShortFormat implements Format<Short> {

    public String format(Short object) throws Exception {
        return object.toString();
    }

    public Short parse(String string) throws Exception {
        return new Short(string);
    }

}
