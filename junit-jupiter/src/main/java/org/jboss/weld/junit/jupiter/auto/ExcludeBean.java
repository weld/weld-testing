package org.jboss.weld.junit.jupiter.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Produces;

/**
 * {@code @ExcludeBean} excludes a bean, or multiple beans, that include a bean defining annotation
 * (e.g. scope) from automatic discovery. This can be helpful to allow replacing a bean class with a different
 * implementation, typically a mock.
 *
 * <p>
 * The type of bean to exclude is implied by the annotated fields' type or annotated methods' return type. If the type
 * is a base class or interface all beans extending / implementing that type will be excluded.
 *
 * <p>
 * NOTE: This annotation will only exclude beans defined by class annotations. It will not exclude beans of the
 * implied type that are defined by {@link Produces} producer methods / fields or synthetic
 * beans. Also, current implementation excludes beans based on type, disregarding any qualifiers that are specified.
 *
 * <p>
 * <b>Example:</b>
 *
 * <pre>
 * &#64;EnableAutoWeld
 * class TestSomeFoo {
 *
 *     &#64;Inject
 *     SomeFoo someFoo; // SomeFoo depends upon application scoped bean Foo
 *
 *     &#64;Produces
 *     &#64;ExcludeBean // Excludes beans with type Foo from automatic discovery
 *     Foo mockFoo = mock(Foo.class); // mockFoo is now produced in place of original Foo impl
 *
 *     &#64;Test
 *     void test(Foo myFoo) {
 *         assertNotNull(myFoo);
 *         assertEquals(myFoo.getBar(), "mock-foo");
 *     }
 * }
 * </pre>
 *
 * @see ExcludeBeanClasses
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Inherited
public @interface ExcludeBean {
}
