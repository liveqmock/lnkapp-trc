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
public @interface SeperatedTextMessage {
    String separator() default "\\|";
    String quote() default "";
    boolean mainClass() default false;
}
