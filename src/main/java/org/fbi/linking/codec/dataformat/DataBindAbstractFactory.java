package org.fbi.linking.codec.dataformat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.*;

/**
 * User: zhanrui
 * Date: 13-9-7
 */
public abstract class DataBindAbstractFactory implements DataBindFactory {
    private static final transient Logger logger = LoggerFactory.getLogger(DataBindAbstractFactory.class);

    protected final Map<String, List<Field>> annotatedLinkFields = new LinkedHashMap<String, List<Field>>();
    protected Set<Class<?>> models;

    private AnnotationModelLoader modelsLoader;
    private String[] packageNames;
    protected String charset;


    public DataBindAbstractFactory(String... packageNames) throws Exception {
        this.packageNames = packageNames;
        this.modelsLoader = new AnnotationModelLoader();

        if (logger.isDebugEnabled()) {
            for (String str : this.packageNames) {
                logger.debug("Package name: {}", str);
            }
        }

        initModel();
    }

    public void initModel() throws Exception {
        initModelClasses(this.packageNames);
    }

    private void initModelClasses(String... packageNames) throws Exception {
        models = modelsLoader.loadModels(packageNames);
    }

//    public abstract void initAnnotatedFields() throws Exception;
//    public abstract void bind(List<String> data, Map<String, Object> model, int line) throws Exception;
//    public abstract String unbind(Map<String, Object> model) throws Exception;

    public void link(Map<String, Object> model) throws Exception {
        for (String link : annotatedLinkFields.keySet()) {
            List<Field> linkFields = annotatedLinkFields.get(link);

            for (Field field : linkFields) {
                field.setAccessible(true);

                String toClassName = field.getType().getName();
                Object to = model.get(toClassName);

                field.set(model.get(field.getDeclaringClass().getName()), to);
            }
        }
    }

    //生成对应的实例
    public Map<String, Object> factory() throws Exception {
        Map<String, Object> mapModel = new HashMap<String, Object>();

        for (Class<?> cl : models) {
            Object obj = cl.newInstance();
            mapModel.put(obj.getClass().getName(), obj);
        }

        return mapModel;
    }

    protected static Integer generateKey(Integer key1, Integer key2) {
        String key2Formatted;
        String keyGenerated;

        if ((key1 != null) && (key2 != null)) {
            key2Formatted = getNumberFormat().format((long) key2);
            keyGenerated = String.valueOf(key1) + key2Formatted;
        } else {
            throw new IllegalArgumentException("顺序号未定义..");
        }

        return Integer.valueOf(keyGenerated);
    }

    private static NumberFormat getNumberFormat() {
        NumberFormat nf = NumberFormat.getInstance();

        nf.setMaximumIntegerDigits(3);
        nf.setMinimumIntegerDigits(3);

        return nf;
    }

    public static Object getDefaultValueForPrimitive(Class<?> clazz) throws Exception {
        if (clazz == byte.class) {
            return Byte.MIN_VALUE;
        } else if (clazz == short.class) {
            return Short.MIN_VALUE;
        } else if (clazz == int.class) {
            return Integer.MIN_VALUE;
        } else if (clazz == long.class) {
            return Long.MIN_VALUE;
        } else if (clazz == float.class) {
            return Float.MIN_VALUE;
        } else if (clazz == double.class) {
            return Double.MIN_VALUE;
        } else if (clazz == char.class) {
            return Character.MIN_VALUE;
        } else if (clazz == boolean.class) {
            return false;
        } else {
            return null;
        }

    }

    @SuppressWarnings("unchecked")
    public String formatString(Format format, Object value) throws Exception {
        String strValue = "";

        if (value != null) {
            try {
                strValue = format.format(value);
            } catch (Exception e) {
                throw new IllegalArgumentException("格式化错误:" + value, e);
            }
        }

        return strValue;
    }

    public static char getCharDelimitor(String separator) {
        if (separator.equals("\\u0001")) {
            return '\u0001';
        } else if (separator.equals("\\t") || separator.equals("\\u0009")) {
            return '\u0009';
        } else if (separator.length() > 1) {
            return separator.charAt(separator.length() - 1);
        } else {
            return separator.charAt(0);
        }
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
