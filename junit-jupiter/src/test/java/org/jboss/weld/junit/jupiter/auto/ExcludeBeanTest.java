package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.inject.Produces;

import org.jboss.weld.junit.jupiter.basic.Foo;
import org.jboss.weld.junit.jupiter.basic.SomeFoo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddBeanClasses(SomeFoo.class)
class ExcludeBeanTest {

    @Produces
    @ExcludeBean
    Foo fakeFoo = new Foo("non-baz");

    @Test
    @DisplayName("Ensure @ExcludeBean excludes the implied bean class")
    void test(Foo myFoo) {
        assertNotNull(myFoo);
        assertEquals(myFoo.getBar(), "non-baz");
    }

}
