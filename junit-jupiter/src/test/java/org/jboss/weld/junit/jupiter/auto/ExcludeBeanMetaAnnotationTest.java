package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Produces;

import org.jboss.weld.junit.jupiter.basic.Foo;
import org.jboss.weld.junit.jupiter.basic.SomeFoo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Ensures that @ExcludeBean can be used as meta annotation and weld-testing should still process it correctly.
 * See also https://github.com/weld/weld-testing/issues/223
 */
@EnableAutoWeld
@AddBeanClasses(SomeFoo.class)
public class ExcludeBeanMetaAnnotationTest {

    @Produces
    @MyMetaAnnotation2
    Foo fakeFoo = new Foo("non-baz");

    @Test
    @DisplayName("Ensure @ExcludeBean is respected even as meta-annotation")
    void test(Foo myFoo) {
        assertNotNull(myFoo);
        assertEquals(myFoo.getBar(), "non-baz");
    }

    @ExcludeBean
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    @interface MyMetaAnnotation {
    }

    @MyMetaAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.METHOD })
    @interface MyMetaAnnotation2 {
    }
}
