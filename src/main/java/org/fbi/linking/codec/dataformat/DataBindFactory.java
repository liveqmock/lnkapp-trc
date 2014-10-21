package org.fbi.linking.codec.dataformat;

import java.util.List;
import java.util.Map;

/**
 * User: zhanrui
 * Date: 13-9-7
 */
public interface DataBindFactory {

    void initModel() throws Exception;

    /**
     * 将通讯报文解析成Object
     * @param tokens  报文域的集合
     * @param modelMap  生成的ObjectMap
     * @throws Exception
     */
    void bind(List<String> tokens, Map<String, Object> modelMap) throws Exception;

    /**
     * 将通讯报文解析成Object
     * @param record    报文体（未处理成报文域集合前的原始报文体）
     * @param modelMap    生成的ObjectMap
     * @throws Exception
     */
    void bind(String record, Map<String, Object> modelMap) throws Exception;
    void bind(byte[] buffer, Map<String, Object> modelMap) throws Exception;
    String unbind(Map<String, Object> modelMap) throws Exception;
}

