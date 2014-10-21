package org.fbi.linking.codec.dataformat;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-6
 */
public interface DataFormat {
    Object toMessage(Object o) throws Exception;
    Object fromMessage(Object o) throws Exception;
}
