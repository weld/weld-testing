package org.jboss.weld.junit5.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Produces;

/**
 * {@code @ExcludeBeanClasses} excludes a set of classes with bean defining annotations (e.g. scopes) from automatic
 * discovery. This can be helpful to allow replacing bean classes with a different implementation, typically a mock.
 *
 * <p>This annotation works as an inverse of {@link AddBeanClasses} hence usually requires actual bean implementation
 * classes as parameters.
 *
 * <p>NOTE: This annotation will only exclude beans defined by class annotations. It will not exclude beans of the
 * specified type that are defined by {@link Produces} producer methods / fields or synthetic beans.
 *
 * <p><b>Example:</b>
 * <pre>
 * &#64;EnableAutoWeld
 * &#64;ExcludeBeanClasses(Foo.class)     // Excludes Foo bean class from automatic discovery
 * class TestSomeFoo {
 *
 *   &#64;Inject
 *   SomeFoo someFoo;                 // SomeFoo depends upon application scoped bean Foo
 *
 *   &#64;Produces
 *   Foo mockFoo = mock(Foo.class);   // mockFoo is now produced in place of original Foo impl
 *
 *   &#64;Test
 *   void test(Foo myFoo) {
 *     assertNotNull(myFoo);
 *     assertEquals(myFoo.getBar(), "mock-foo");
 *   }
 * }
 * </pre>
 *
 * @see ExcludeBean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Repeatable(ExcludeBeanClasses.All.class)
public @interface ExcludeBeanClasses {

    Class<?>[] value();

    /**
     * Container annotation for repeatable {@link ExcludeBeanClasses}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    @interface All {
        ExcludeBeanClasses[] value();
    }

}
