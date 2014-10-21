package org.fbi.linking.codec.dataformat;

import org.fbi.linking.codec.dataformat.annotation.*;
import org.fbi.linking.codec.dataformat.format.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-7
 */
public class FixedLengthTextDataBindFactory extends DataBindAbstractFactory implements DataBindFactory {
    private static final transient Logger LOG = LoggerFactory.getLogger(FixedLengthTextDataBindFactory.class);

    private Map<Integer, DataField> dataFields = new LinkedHashMap<Integer, DataField>();
    private Map<Integer, Field> annotatedFields = new LinkedHashMap<Integer, Field>();

    //本交易的主控类
    private Class mainClass;
    //对应本交易中重复记录的定义类
    private Class repeatClass;

    //定义重复记录总条数的字段名称
    private String repeatRecordNumberFieldName;
    //重复记录对应的类的注解域集合
    private Map<Integer, DataField> oneToMangDataFields = new LinkedHashMap<Integer, DataField>();
    private Map<Integer, Field> oneToMangAnnotatedFields = new LinkedHashMap<Integer, Field>();

    public FixedLengthTextDataBindFactory(String... packageNames) throws Exception {
        super(packageNames);
        init();
    }

    private void init() {
        initAnnotatedFields();
        initParameters();
    }

    public void initAnnotatedFields() {
        for (Class<?> cl : models) {
            List<Field> linkFields = new ArrayList<Field>();

            for (Field field : cl.getDeclaredFields()) {
                DataField dataField = field.getAnnotation(DataField.class);
                if (dataField != null) {
                    dataFields.put(dataField.seq(), dataField);
                    annotatedFields.put(dataField.seq(), field);
                }

                Link linkField = field.getAnnotation(Link.class);
                if (linkField != null) {
                    linkFields.add(field);
                }

                OneToMany oneToManyField = field.getAnnotation(OneToMany.class);
                if (oneToManyField != null) {
                    repeatRecordNumberFieldName = oneToManyField.totalNumberField();
                }
            }

            if (!linkFields.isEmpty()) {
                annotatedLinkFields.put(cl.getName(), linkFields);
            }
        }
    }

    private void initOneToManyAnnotatedFields(Class cl) {
        for (Field field : cl.getDeclaredFields()) {
            DataField dataField = field.getAnnotation(DataField.class);
            if (dataField != null) {
                oneToMangDataFields.put(dataField.seq(), dataField);
                oneToMangAnnotatedFields.put(dataField.seq(), field);
            }
        }
    }


    public void bind(List<String> tokens, Map<String, Object> modelMap) throws Exception {
        throw new RuntimeException("暂不支持报文域集合参数形式的处理方式.");
    }

    public void bind(String record, Map<String, Object> modelMap) throws Exception {
        throw new RuntimeException("暂不支持报文域集合参数形式的处理方式.");
    }

    public void bind(byte[] buf, Map<String, Object> modelMap) throws Exception {
        Map<String, List<Object>> oneToManyModelMap = new HashMap<String, List<Object>>();
        bind(buf, modelMap, oneToManyModelMap);
    }

    public void bind(byte[] buf, Map<String, Object> modelMap, Map<String, List<Object>> oneToManyModelMap) throws Exception {
        List<String> tokens = new ArrayList<>();
        DataField dataField;
        Field field;
        String token;
        int offset = 0;
        int length;
        int repeatRecordNumber = 0;   //报文中重复记录的条数

        Iterator itr = dataFields.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            dataField = (DataField) entry.getValue();
            int dataFieldKey = (int) entry.getKey();
            field = annotatedFields.get(dataFieldKey);
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (oneToMany != null) {
                if (repeatRecordNumber == 0) {
                    //throw new RuntimeException("OneToMany报文定义中缺少对应的totalNumberField.");

                    //repeatRecordNumber为0时，添加一个空的token
                    tokens.add("");
                }
                length = dataField.length();
                for (int i = 0; i < repeatRecordNumber; i++) {
                    byte[] fieldBytes = new byte[length];
                    System.arraycopy(buf, offset, fieldBytes, 0, length);
                    token = new String(fieldBytes, charset);
                    //if (dataField.trim()) {
                    //    tokens.add(token.trim());
                    //} else {
                    tokens.add(token);
                    //}
                    offset += length;
                }
            } else {
                length = dataField.length();
                byte[] fieldBytes = new byte[length];
                System.arraycopy(buf, offset, fieldBytes, 0, length);
                token = new String(fieldBytes, charset);
                if (dataField.trim()) {
                    tokens.add(token.trim());
                } else {
                    tokens.add(token);
                }

                //获取重复记录的总条数
                if (repeatRecordNumberFieldName != null) {
                    if (field.getName().equals(repeatRecordNumberFieldName)) {
                        repeatRecordNumber = Integer.parseInt(token.trim());
                    }
                }
                offset += length;
            }
        }

        if (offset != buf.length) {
            throw new RuntimeException("报文各个域定义的长度之和" + offset + "与报文总长度" + buf.length + "不符。");
        }

        //
        for (Class clazz : models) {
            Object obj = modelMap.get(clazz.getName());
            if (obj != null) {
                generateModelMap(clazz, obj, tokens, oneToManyModelMap);
            }
        }
    }


    private void generateModelMap(Class clazz, Object model, List<String> tokens, Map<String, List<Object>> oneToManyModelMap) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            DataField dataField = field.getAnnotation(DataField.class);
            if (dataField != null) {
                OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                if (oneToMany == null) { //普通DataField域
                    if (model == null) { //OneToMany的情况
                        List<Object> oneToManyModels = oneToManyModelMap.get(clazz.getName());
                        int colsSeq = dataField.seq() - 1;
                        for (int i = 0; i < tokens.size(); i++) {
                            List<String> colsList = splitOnetomanyRecord(tokens.get(i).getBytes(charset));
                            if (colsList.size() == 0 || colsList.isEmpty()) {
                                throw new IllegalArgumentException("OneToMany字段值不能为空。");
                            }

                            if ((!oneToManyModels.isEmpty()) && (oneToManyModels.size() > i)) {
                                model = oneToManyModels.get(i);
                            } else {
                                model = clazz.newInstance();
                            }

                            String colValue = colsList.get(colsSeq);
                            String pattern = dataField.pattern();

                            Format<?> format = FormatFactory.getFormat(field.getType(), pattern, dataField.precision());
                            Object value = null;
                            if (!colValue.equals("")) {
                                try {
                                    value = format.parse(colValue);
                                } catch (FormatException fe) {
                                    throw new IllegalArgumentException(fe.getMessage() + ", data: " + colValue, fe);
                                } catch (Exception e) {
                                    throw new IllegalArgumentException("Parsing error: " + colValue, e);
                                }
                            } else {
                                value = getDefaultValueForPrimitive(field.getType());
                            }
                            field.set(model, value);

                            if ((!oneToManyModels.isEmpty()) && (oneToManyModels.size() > i)) {
                                oneToManyModels.set(i, model);
                            } else {
                                oneToManyModels.add(i, model);
                            }
                            oneToManyModelMap.put(clazz.getName(), oneToManyModels);
                            model = null;
                        }
                    } else {
                        String data = tokens.get(dataField.seq() - 1);
                        if (dataField.trim()) {
                            data = data.trim();
                        }

                        Format<?> format;
                        String pattern = dataField.pattern();
                        format = FormatFactory.getFormat(field.getType(), pattern, dataField.precision());
                        Object value = null;
                        if (!data.equals("")) {
                            try {
                                value = format.parse(data);
                            } catch (FormatException fe) {
                                throw new IllegalArgumentException(fe.getMessage() + ", data: " + data, fe);
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Parsing error: " + data, e);
                            }
                        } else {
                            value = getDefaultValueForPrimitive(field.getType());
                        }
                        field.set(model, value);
                    }
                } else { //OneToMany
                    String targetClass = oneToMany.mappedTo();
                    if (!targetClass.equals("")) {
                        Class cl = null;
                        try {
                            cl = Class.forName(targetClass);
                            //cl = Thread.currentThread().getContextClassLoader().loadClass(targetClass);   //osgi
                        } catch (ClassNotFoundException e) {
                            cl = getClass().getClassLoader().loadClass(targetClass);
                            //TODO
                        }
                        //初始化OneToMany Fields定义
                        initOneToManyAnnotatedFields(cl);

                        String totalNumberFieldName = oneToMany.totalNumberField();
                        Field totalNumberField = clazz.getDeclaredField(totalNumberFieldName);
                        totalNumberField.setAccessible(true);
                        int totalNumber = Integer.parseInt((String) totalNumberField.get(model));

                        //if (totalNumber > 0) {
                        int beginIdx = dataField.seq() - 1;
                        int endIdx = beginIdx + totalNumber;
                        if (endIdx > tokens.size()) {
                            endIdx = tokens.size();
                        }
                        List<String> oneToManyFieldDataList = tokens.subList(beginIdx, endIdx);

                        if (!oneToManyModelMap.containsKey(cl.getName())) {
                            oneToManyModelMap.put(cl.getName(), new ArrayList<Object>());
                        }

                        generateModelMap(cl, null, oneToManyFieldDataList, oneToManyModelMap);
                        field.set(model, oneToManyModelMap.get(cl.getName()));
                        //}
                    } else {
                        throw new RuntimeException("OneToMany defined error.");
                    }
                }
            }
        }
    }

    //将一条OneToMany记录分割成字段数组
    private List<String> splitOnetomanyRecord(byte[] recordBuffer) throws UnsupportedEncodingException {
        List<String> tokens = new ArrayList<>();
        DataField dataField;
        String token;
        int offset = 0;
        int length;

        for (Map.Entry<Integer, DataField> integerDataFieldEntry : oneToMangDataFields.entrySet()) {
            Map.Entry entry = (Map.Entry) integerDataFieldEntry;
            dataField = (DataField) entry.getValue();
            length = dataField.length();
            byte[] fieldBytes = new byte[length];
            System.arraycopy(recordBuffer, offset, fieldBytes, 0, length);
            token = new String(fieldBytes, charset);
            if (dataField.trim()) {
                tokens.add(token.trim());
            } else {
                tokens.add(token);
            }
            offset += length;
        }

        if (offset != recordBuffer.length) {
            throw new RuntimeException("OneToMany报文各个域定义的长度之和" + offset
                    + "与报文总长度(" + recordBuffer.length + ")不符。");
        }
        return tokens;
    }

    @Override
    public String unbind(Map<String, Object> modelsMap) throws Exception {
        StringBuilder buffer = new StringBuilder();
        Map<Integer, List<String>> results = new HashMap<Integer, List<String>>();

        for (Class clazz : models) {
            if (modelsMap.containsKey(clazz.getName())) {
                Object obj = modelsMap.get(clazz.getName());
                if (obj != null) {
                    generateMessagePositionMap(clazz, obj, results);
                }
            }
        }

        //处理results
        TreeMap<Integer, List> sortValues = new TreeMap<Integer, List>(results);
        List<String> tokens = new ArrayList<String>();

        for (Map.Entry<Integer, List> entry : sortValues.entrySet()) {
            List<String> values = (List<String>) entry.getValue();
            for (String value : values) {
                if (value != null) {
                    tokens.add(value);
                } else {
                    tokens.add("");
                }
            }
        }

        Iterator itx = tokens.iterator();
        while (itx.hasNext()) {
            String res = (String) itx.next();
            if (res != null) {
                buffer.append(res);
            }
        }
        return buffer.toString();
    }

    private void generateMessagePositionMap(Class clazz, Object obj, Map<Integer, List<String>> resultsMap) throws Exception {
        String fieldStrVal = "";
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            DataField datafield = field.getAnnotation(DataField.class);
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (datafield != null && oneToMany == null) {
                if (obj != null) {
                    Class type = field.getType();
                    String pattern = datafield.pattern();
                    int precision = datafield.precision();

                    Format format = FormatFactory.getFormat(type, pattern, precision);
                    Object value = field.get(obj);
                    fieldStrVal = formatString(format, value);

                    if (datafield.trim()) {
                        fieldStrVal = fieldStrVal.trim();
                    }

                    //format for fixed length field
                    fieldStrVal = formatFixedLengthField(fieldStrVal, datafield);
                } else {
                    fieldStrVal = "";
                }
                Integer key = datafield.seq();

                if (!resultsMap.containsKey(key)) {
                    List<String> list = new LinkedList<String>();
                    list.add(fieldStrVal);
                    resultsMap.put(key, list);
                } else {
                    List<String> list = resultsMap.get(key);
                    list.add(fieldStrVal);
                }
            }

            if (oneToMany != null) {
                ArrayList list = (ArrayList) field.get(obj);
                if (list != null) {
                    Class<?> aClass = list.get(0).getClass();
                    OneToManyFixedLengthTextMessage oneToManyTextMessage = aClass.getAnnotation(OneToManyFixedLengthTextMessage.class);
                    if (oneToManyTextMessage == null) {
                        throw new RuntimeException("OneToManyFixedLengthTextMessage not defined!");
                    }
                    //TODO 使用OneToManyFixedLengthTextMessage中的align等定义进行处理

                    //初始化OneToMany Fields定义
                    initOneToManyAnnotatedFields(aClass);

                    for (Object target : list) {
                        StringBuilder oneToManyResultBuffer = new StringBuilder();

                        Map<Integer, String> oneToManyResultsMap = new HashMap<Integer, String>();
                        generateOneToManyMessagePositionMap(target.getClass(), target, oneToManyResultsMap);

                        //转换成字符串
                        TreeMap<Integer, String> sortValues = new TreeMap<Integer, String>(oneToManyResultsMap);
                        Iterator itx = sortValues.entrySet().iterator();
                        while (itx.hasNext()) {
                            Map.Entry<Integer, String> entry = (Map.Entry<Integer, String>) itx.next();
                            String res = entry.getValue();
                            oneToManyResultBuffer.append(formatFixedLengthField(res, oneToMangDataFields.get(entry.getKey())));
                        }

                        //回填到resultsMap中
                        Integer key = datafield.seq();
                        if (!resultsMap.containsKey(key)) {
                            List<String> listTmp = new LinkedList<String>();
                            listTmp.add(oneToManyResultBuffer.toString());
                            resultsMap.put(key, listTmp);
                        } else {
                            List<String> listTmp = resultsMap.get(key);
                            listTmp.add(oneToManyResultBuffer.toString());
                        }
                    }
                }
            }
        }
    }

    private String formatFixedLengthField(String fieldStrVal, DataField datafield) throws UnsupportedEncodingException {
        int length = datafield.length();
        char padchar = datafield.padchar();
        String align = datafield.align();
        byte[] fieldByteVal = fieldStrVal.getBytes(charset);

        if (fieldByteVal.length < length) {
            int needpadsize = length - fieldByteVal.length;
            byte[] needpadbuf = new byte[needpadsize];
            for (int i = 0; i < needpadsize; i++) {
                needpadbuf[i] = (byte) padchar;  //only ascii
            }
            byte[] fieldtmpbuf = new byte[length];
            if ("R".equals(align)) {
                System.arraycopy(needpadbuf, 0, fieldtmpbuf, 0, needpadbuf.length);
                System.arraycopy(fieldByteVal, 0, fieldtmpbuf, needpadbuf.length, fieldByteVal.length);
            } else {
                System.arraycopy(fieldByteVal, 0, fieldtmpbuf, 0, fieldByteVal.length);
                System.arraycopy(needpadbuf, 0, fieldtmpbuf, fieldByteVal.length, needpadbuf.length);
            }
            fieldStrVal = new String(fieldtmpbuf, charset);
        }

        if (fieldByteVal.length > length) {
            byte[] tmp = new byte[length];
            System.arraycopy(fieldByteVal, 0, tmp, 0, length);
            fieldStrVal = new String(tmp, charset);
        }

        return fieldStrVal;
    }


    private void generateOneToManyMessagePositionMap(Class clazz, Object obj, Map<Integer, String> resultsMap) throws Exception {
        String result = "";
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            DataField datafield = field.getAnnotation(DataField.class);
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (datafield != null && oneToMany == null) {
                if (obj != null) {
                    Class type = field.getType();
                    String pattern = datafield.pattern();
                    int precision = datafield.precision();

                    Format format = FormatFactory.getFormat(type, pattern, precision);
                    Object value = field.get(obj);
                    result = formatString(format, value);

                    if (datafield.trim()) {
                        result = result.trim();
                    }
                } else {
                    result = "";
                }
                Integer key = datafield.seq();

                if (!resultsMap.containsKey(key)) {
                    resultsMap.put(key, result);
                } else {
                    throw new RuntimeException("字段键值重复！");
                }
            }
        }
    }


    //=============
    private void initParameters() {
        for (Class<?> cl : models) {
            FixedLengthTextMessage record = cl.getAnnotation(FixedLengthTextMessage.class);
            if (record != null) {
                boolean isMainClass = record.mainClass();
                if (isMainClass) {
                    this.mainClass = cl;
                }
            }
        }
    }

}
