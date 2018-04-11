package org.jboss.weld.junit5.auto;


import javax.enterprise.inject.spi.Extension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Adds the listed classes as portable CDI extensions to the deployed testing container.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddExtensions.All.class)
public @interface AddExtensions {

    Class<? extends Extension>[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface All {
        AddExtensions[] value();
    }

}
