package org.jboss.weld.junit5.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Adds, and enables, the listed classes as CDI decorators to the deployed testing container.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddEnabledDecorators.All.class)
public @interface AddEnabledDecorators {

    Class<?>[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface All {
        AddEnabledDecorators[] value();
    }

}
