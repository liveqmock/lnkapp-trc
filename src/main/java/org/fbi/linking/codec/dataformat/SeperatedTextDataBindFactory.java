package org.fbi.linking.codec.dataformat;

import org.fbi.linking.codec.dataformat.annotation.*;
import org.fbi.linking.codec.dataformat.format.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-7
 */
public class SeperatedTextDataBindFactory extends DataBindAbstractFactory implements DataBindFactory {
    private static final transient Logger LOG = LoggerFactory.getLogger(SeperatedTextDataBindFactory.class);

    private Map<Integer, DataField> dataFields = new LinkedHashMap<Integer, DataField>();
    private Map<Integer, Field> annotatedFields = new LinkedHashMap<Integer, Field>();

    private String separator;
    private String quote;

    public SeperatedTextDataBindFactory(String... packageNames) throws Exception {
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
            }

            if (!linkFields.isEmpty()) {
                annotatedLinkFields.put(cl.getName(), linkFields);
            }
        }
    }


    public void bind(String record, Map<String, Object> modelMap) throws Exception {
        throw new RuntimeException("暂不支持此种参数形式的处理方式.");
    }
    public void bind(byte[] buf, Map<String, Object> modelMap) throws Exception {
        throw new RuntimeException("暂不支持此种参数形式的处理方式.");
    }
    public void bind(List<String> tokens, Map<String, Object> modelMap) throws Exception {
        Map<String, List<Object>> oneToManyModelMap = new HashMap<String, List<Object>>();
        bind(tokens, modelMap, oneToManyModelMap);
    }

    public void bind(List<String> tokens, Map<String, Object> modelMap, Map<String, List<Object>> oneToManyModelMap) throws Exception {
        for (Class clazz : models) {
            Object obj = modelMap.get(clazz.getName());
            if (obj != null) {
                generateModelMap(clazz, obj, tokens, oneToManyModelMap);
            }
        }
    }

    public void generateModelMap(Class clazz, Object model, List<String> tokens, Map<String, List<Object>> oneToManyModelMap) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            DataField dataField = field.getAnnotation(DataField.class);
            if (dataField != null) {
/*
                String data = tokens.get(dataField.seq() - 1);
                if (dataField.trim()) {
                    data = data.trim();
                }
*/

                OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                if (oneToMany == null) { //普通DataField域
                    if (model == null) { //OneToMany的情况
                        //SeperatedTextMessage seperatedTextMessage = (SeperatedTextMessage) clazz.getAnnotation(SeperatedTextMessage.class);
                        OneToManySeperatedTextMessage seperatedTextMessage = (OneToManySeperatedTextMessage) clazz.getAnnotation(OneToManySeperatedTextMessage.class);
                        if (seperatedTextMessage == null) {
                            throw new RuntimeException("OntToManySeperatedTextMessage not defined!");
                        }
                        String oneToManySeparator = seperatedTextMessage.separator();
                        List<Object> oneToManyModels = oneToManyModelMap.get(clazz.getName());
                        int colsSeq = dataField.seq() - 1;
                        for (int i = 0; i < tokens.size(); i++) {
                            String[] colsArray = tokens.get(i).split(oneToManySeparator);
                            List<String> colsList = Arrays.asList(colsArray);
                            if (colsList.size() == 0 || colsList.isEmpty()) {
                                throw new IllegalArgumentException("Field Data String is empty!");
                            }

                            if ((!oneToManyModels.isEmpty()) && (oneToManyModels.size() > i)) {
                                model = oneToManyModels.get(i);
                            } else {
                                model = clazz.newInstance();
                            }

                            String colValue = (String) colsList.get(colsSeq);
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
                        String totalNumberFieldName = oneToMany.totalNumberField();
                        Field totalNumberField = clazz.getDeclaredField(totalNumberFieldName);
                        totalNumberField.setAccessible(true);
                        int totalNumber = Integer.parseInt((String) totalNumberField.get(model));
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
                    } else {
                        throw new RuntimeException("OneToMany defined error.");
                    }
                }
            }

        }
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
            if (itx.hasNext()) {
                buffer.append(getCharDelimitor(separator));
            }
        }
        return buffer.toString();
    }

    private void generateMessagePositionMap(Class clazz, Object obj, Map<Integer, List<String>> resultsMap) throws Exception {
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
                    List<String> list = new LinkedList<String>();
                    list.add(result);
                    resultsMap.put(key, list);
                } else {
                    List<String> list = resultsMap.get(key);
                    list.add(result);
                }
            }

            if (oneToMany != null) {
                String oneToManySeparator = null;
                ArrayList list = (ArrayList) field.get(obj);
                if (list != null) {
                    Iterator it = list.iterator();
                    while (it.hasNext()) {
                        StringBuilder oneToManyResultBuffer = new StringBuilder();
                        Object target = it.next();
                        if (oneToManySeparator == null) {
                            //SeperatedTextMessage seperatedTextMessage = (SeperatedTextMessage) target.getClass().getAnnotation(SeperatedTextMessage.class);
                            OneToManySeperatedTextMessage seperatedTextMessage = (OneToManySeperatedTextMessage) target.getClass().getAnnotation(OneToManySeperatedTextMessage.class);
                            if (seperatedTextMessage == null) {
                                throw new RuntimeException("OntToManySeperatedTextMessage not defined!");
                            }
                            oneToManySeparator = seperatedTextMessage.separator();
                        }

                        Map<Integer, String> oneToManyResultsMap = new HashMap<Integer, String>();
                        generateOneToManyMessagePositionMap(target.getClass(), target, oneToManyResultsMap);

                        //转换成字符串
                        TreeMap<Integer, String> sortValues = new TreeMap<Integer, String>(oneToManyResultsMap);
                        Iterator itx = sortValues.entrySet().iterator();
                        while (itx.hasNext()) {
                            Map.Entry<Integer, String> entry = (Map.Entry<Integer, String>) itx.next();
                            String res = entry.getValue();
                            if (res != null) {
                                oneToManyResultBuffer.append(res);
                            } else {
                                oneToManyResultBuffer.append(""); //待测
                            }
                            if (itx.hasNext()) {
                                oneToManyResultBuffer.append(oneToManySeparator);
                            }
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
        if (separator == null) {
            for (Class<?> cl : models) {
                SeperatedTextMessage record = cl.getAnnotation(SeperatedTextMessage.class);
                if (record != null) {
                    boolean isMainClass = record.mainClass();
                    if (isMainClass) {
                        separator = record.separator();
                        quote = record.quote();
                    }
                }
            }
        }
    }


    //==============

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
