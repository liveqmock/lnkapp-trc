
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

    //指定含有合计笔数的字段名称
    String totalNumberField() default "";

    //指定含有合计金额的字段名称
    String totalAmtField() default "";
}
