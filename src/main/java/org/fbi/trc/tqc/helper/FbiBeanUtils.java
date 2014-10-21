package org.fbi.trc.tqc.helper;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by zhanrui on 13-12-31.
 */
public class FbiBeanUtils {
    private static String FIELD_DATE_FORMAT = "yyyy-MM-dd";
    private static String[] FIELD_TYPE_SIMPLE = {"java.lang.String","java.lang.Integer", "int", "java.util.Date", "java.math.BigDecimal"};
    private static String FIELD_TYPE_INTEGER = "java.lang.Integer,int";
    private static String FIELD_TYPE_DATE = "java.util.Date";
    private static String FIELD_TYPE_BIGDECIMAL = "java.math.BigDecimal";

    public static void copyProperties(Map srcMap, Object targetObj) throws Exception {
        FbiBeanUtils.copyProperties(srcMap, targetObj, false);
    }

    /**
     * Map -> Bean
     * @param isToCamel 是否将带下划线的字段名称 转换为 camel形式的名称
     */
    public static void copyProperties(Map srcMap, Object targetBean, boolean isToCamel)  {
        try {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(targetBean.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor prop : propertyDescriptors) {
                Method writeMethod = prop.getWriteMethod();
                if (writeMethod != null) {
                    for (Object obj : srcMap.keySet()) {
                        String mapKey = (String) obj;
                        String mapkeyCamel = isToCamel ? toCamel(mapKey.toLowerCase()) : mapKey;
                        String beanPropName = isToCamel ? toCamel(prop.getName()) : prop.getName();
                        if (mapkeyCamel.equals(beanPropName)) {
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            Object value = srcMap.get(mapKey);
                            if (value == null) {
                                break;
                            }
                            //类型不匹配则转换
                            if (!(prop.getPropertyType().getName().equals(value.getClass().getName()))) {
                                value = parseByType(prop.getPropertyType(), value.toString());
                            }
                            writeMethod.invoke((Object) targetBean, new Object[]{value});
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Map->Bean copy 错误.", e);
        }
    }



    public static void copyProperties(Object source, Object target) {
        FbiBeanUtils.copyProperties(source, target, false);
    }

    /**
     * Bean -> Bean
     * @param isToCamel 是否将带下划线的字段名称 转换为 camel形式的名称
     */
    public static void copyProperties(Object source, Object target, boolean isToCamel) {
        try {
            PropertyDescriptor[] targetProps = Introspector.getBeanInfo(target.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor targetProp : targetProps) {
                Method writeMethod = targetProp.getWriteMethod();
                if (writeMethod != null) {
                    PropertyDescriptor[] srcProps = Introspector.getBeanInfo(source.getClass()).getPropertyDescriptors();
                    for (PropertyDescriptor srcProp : srcProps) {
                        String srcPropName = isToCamel ? toCamel(srcProp.getName()) : srcProp.getName();
                        String targetPropName = isToCamel ? toCamel(targetProp.getName()) : targetProp.getName();
                        if (srcPropName.equals(targetPropName)) {
                            Method readMethod = srcProp.getReadMethod();
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source, new Object[0]);
                            if (value == null) {
                                break;
                            }
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            //类型不匹配则转换
                            if (!(targetProp.getPropertyType().getName().equals(value.getClass().getName()))) {
                                value = parseByType(targetProp.getPropertyType(), value);
                            }

                            writeMethod.invoke((Object) target, new Object[]{value});
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Bean->Bean copy error.", e);
        }
    }

    //带下划线的字段名改为camel型
    //map->bean时使用
    private static String toCamel(String srcStr) {
        //TODO 可先判断name中是否存在下划线

        StringBuilder sb = new StringBuilder();
        boolean match = false;
        for (int i = 0; i < srcStr.length(); i++) {
            char ch = srcStr.charAt(i);
            if (match && ch >= 97 && ch <= 122)
                ch -= 32;
            if (ch != '_') {
                match = false;
                sb.append(ch);
            } else {
                match = true;
            }
        }
        return sb.toString();
    }


    private static Object parseObject(Class clazz, String str) throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Object obj;
        if (str == null || str.equals("")) {
            obj = null;
        } else {
            obj = clazz.newInstance();
            Method m = clazz.getMethod("setId", str.getClass());
            m.invoke(obj, str);
        }
        return obj;
    }


    private static Object parseByType(Class targetClazz, Object srcObj) throws ParseException, InstantiationException, IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
        Object obj = "";
        String clazzName = targetClazz.getName().trim();
        if (isSimpleType(clazzName)) {
            String srcStr = srcObj.toString();
            if (FIELD_TYPE_INTEGER.contains(clazzName)) {
                obj = parseInteger(srcStr);
            } else if (FIELD_TYPE_DATE.contains(clazzName)) {
                obj = parseDate(srcStr);
            } else if (FIELD_TYPE_BIGDECIMAL.contains(clazzName)) {
                obj = parseBigDecimal(srcStr);
            } else {
                obj = srcStr;
            }
        } else {
            //obj = parseObject(targetClazz, srcObj);
            //throw new RuntimeException("复杂对象copy暂不支持.");
            obj = srcObj;
        }
        return obj;
    }

    private static boolean isSimpleType(String type) {
        for (int i = 0; i < FIELD_TYPE_SIMPLE.length; i++) {
            if (type.equals(FIELD_TYPE_SIMPLE[i])) {
                return true;
            }
        }
        return false;
    }

    private static Integer parseInteger(String str) {
        if (str == null || str.equals("")) {
            return 0;
        } else {
            return Integer.parseInt(str);
        }
    }

    private static Date parseDate(String str) throws ParseException {
        if (str == null || str.equals("")) {
            return null;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(FIELD_DATE_FORMAT);
            Date date = sdf.parse(str);
            return date;
        }
    }

    private static BigDecimal parseBigDecimal(String str) throws ParseException {
        if (str == null || str.equals("")) {
            return null;
        } else {
            return new BigDecimal(str);
        }
    }

    //====

    public static String getDateFormat() {
        return FIELD_DATE_FORMAT;
    }

    public static void setDateFormat(String DATE_FORMAT) {
        FbiBeanUtils.FIELD_DATE_FORMAT = DATE_FORMAT;
    }
}
