package org.jboss.weld.junit5.auto;


import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Adds all the beans related to a specific classpath entry to the deployed testing container. This annotation
 * makes it relatively easy to add all the bean classes in a library.
 * <p>
 * Classpath entries are selected by providing <i>any</i> bean class in the classpath entry.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddClasspaths.All.class)
public @interface AddClasspaths {

    Class<?>[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface All {
        AddClasspaths[] value();
    }

}
