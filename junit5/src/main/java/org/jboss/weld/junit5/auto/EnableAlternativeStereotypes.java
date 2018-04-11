package org.jboss.weld.junit5.auto;


import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Adds the listed annotations as alternative stereotypes to the deployed container.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(EnableAlternativeStereotypes.All.class)
public @interface EnableAlternativeStereotypes {

    Class<? extends Annotation>[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface All {
        EnableAlternativeStereotypes[] value();
    }

}
