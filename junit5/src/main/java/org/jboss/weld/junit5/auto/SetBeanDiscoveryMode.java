package org.jboss.weld.junit5.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;

/**
 * Sets discovery mode for Weld SE synthetic bean archive. Valid options are {@link BeanDiscoveryMode#ALL} and
 * {@link BeanDiscoveryMode#ANNOTATED}.
 * <p>
 * Starting with Weld 5 (CDI 4), the default value is {@code ANNOTATED}. Applications can leverage this annotation to
 * enforce same behavior as older Weld versions where synthetic archives used discovery mode {@code ALL}.
 * <p>
 * This annotation is equal to invocation of {@link org.jboss.weld.environment.se.Weld#setBeanDiscoveryMode(BeanDiscoveryMode)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface SetBeanDiscoveryMode {

    BeanDiscoveryMode value();
}
