package org.fbi.linking.codec.dataformat.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-6
 */

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataField {
    //˳��� 1��2��3...
    int seq();

    //�ֶ�����
    String name() default "";

    //��ʽת��ģʽ
    String pattern() default "";

    //������ʽʱ�ĳ���
    int length() default 0;

    //������ʽʱ�Ķ��뷽ʽ
    String align() default "L";

    //������ʽʱ�Ĳ��뷽ʽ
    char padchar() default ' ';

    //�Ƿ���trim����
    boolean trim() default true;

    //BogDecimal ����  ����������Ϊ2   -1��������
    int precision() default -1;

    //��ϸѭ�������־
    //int loop() default 0;
}
