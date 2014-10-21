
package org.fbi.linking.codec.dataformat.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-7
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneToMany {

    String mappedTo() default "";

    //ָ�����кϼƱ������ֶ�����
    String totalNumberField() default "";

    //ָ�����кϼƽ����ֶ�����
    String totalAmtField() default "";
}
