package org.jboss.weld.junit5;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Meta-annotation that allows test classes to be extended with <code>&#64;EnableWeld</code> instead of <code>&#64;ExtendWith(WeldJunit5Extension.class)</code>.
 * 
 * <pre>
 * <br>
 * &#64;EnableWeld
 * public class SimpleTest {
 *
 *     // Injected automatically
 *     &#64;Inject
 *     Foo foo;
 *
 *     &#64;Test
 *     public void testFoo() {
 *         // Weld container is started automatically
 *         assertEquals("baz", foo.getBaz());
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:smoyer1@selesy.com">Steve Moyer</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Inherited
@ExtendWith(WeldJunit5Extension.class)
public @interface EnableWeld {

    Class<?>[] testBeans() default {};

    boolean testPackage() default false;

}
