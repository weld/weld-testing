package org.jboss.weld.junit5.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Adds, and enables, the listed classes as CDI interceptors to the deployed testing container.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddEnabledInterceptors.All.class)
public @interface AddEnabledInterceptors {

    Class<?>[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface All {
        AddEnabledInterceptors[] value();
    }

}
