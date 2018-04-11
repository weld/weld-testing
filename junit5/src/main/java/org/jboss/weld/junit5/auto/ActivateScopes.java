package org.jboss.weld.junit5.auto;


import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Activates the listed scopes for the duration of the test.
 * <p>
 * This annotation can be placed on a test method, class or suite, although
 * each scope is activated individually for each test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(ActivateScopes.All.class)
public @interface ActivateScopes {

    Class<? extends Annotation>[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface All {
        ActivateScopes[] value();
    }

}
