package org.jboss.weld.junit5.auto;


import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Enables the listed classes as bean class alternatives in the deployed testing container.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(EnableAlternatives.All.class)
public @interface EnableAlternatives {

    Class<?>[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface All {
        EnableAlternatives[] value();
    }

}
