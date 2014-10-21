package org.fbi.linking.codec.dataformat.format;

public class ShortPatternFormat extends NumberPatternFormat<Short> {

    public ShortPatternFormat() {
        super();
    }

    public ShortPatternFormat(String pattern) {
        super(pattern);
    }

    @Override
    public Short parse(String string) throws Exception {
        return super.getNumberFormat().parse(string).shortValue();
    }
}
