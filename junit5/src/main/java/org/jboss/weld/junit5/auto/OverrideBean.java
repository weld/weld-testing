package org.jboss.weld.junit5.auto;


import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Overrides a bean class that <i>may</i> otherwise be included in the container.
 *
 * Using this annotation provided an easy way to replace a bean class during a
 * test; It is usually used along with {@link javax.enterprise.inject.Produces}.
 */
@Stereotype
@Alternative
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
public @interface OverrideBean {
}
