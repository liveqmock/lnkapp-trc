package org.fbi.linking.codec.dataformat;
/**
 * User: zhanrui
 * Date: 13-9-7
 */
public interface Format<T> {
    String format(T object) throws Exception;
    T parse(String string) throws Exception;
}
