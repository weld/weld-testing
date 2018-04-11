package org.jboss.weld.junit5.auto;


import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Adds all bean classes from the listed packages to the deployed testing container.
 * <p>
 * Packages are selected by providing <i>any</i> bean class in the package.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddPackages.All.class)
public @interface AddPackages {

    Class<?>[] value();

    boolean recursively() default true;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface All {
        AddPackages[] value();
    }

}
