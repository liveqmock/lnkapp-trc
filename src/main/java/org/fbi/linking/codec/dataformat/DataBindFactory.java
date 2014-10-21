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
     * ��ͨѶ���Ľ�����Object
     * @param tokens  ������ļ���
     * @param modelMap  ���ɵ�ObjectMap
     * @throws Exception
     */
    void bind(List<String> tokens, Map<String, Object> modelMap) throws Exception;

    /**
     * ��ͨѶ���Ľ�����Object
     * @param record    �����壨δ����ɱ����򼯺�ǰ��ԭʼ�����壩
     * @param modelMap    ���ɵ�ObjectMap
     * @throws Exception
     */
    void bind(String record, Map<String, Object> modelMap) throws Exception;
    void bind(byte[] buffer, Map<String, Object> modelMap) throws Exception;
    String unbind(Map<String, Object> modelMap) throws Exception;
}

