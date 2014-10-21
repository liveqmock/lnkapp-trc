package org.fbi.linking.codec.dataformat.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-6
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Link {
    LinkType linkType() default LinkType.OneToOne;
}
