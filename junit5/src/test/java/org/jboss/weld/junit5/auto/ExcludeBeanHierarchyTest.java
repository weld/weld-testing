package org.jboss.weld.junit5.auto;

import org.jboss.weld.junit5.basic.Foo;
import org.jboss.weld.junit5.basic.IFoo;
import org.jboss.weld.junit5.basic.SomeIFoo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@EnableAutoWeld
@AddBeanClasses(SomeIFoo.class)
class ExcludeBeanHierarchyTest {

    @Produces
    @ExcludeBean
    IFoo fakeFoo = new Foo("non-baz");

    @Test
    @DisplayName("Ensure @ExcludeBean excludes all beans in implied hierarchy")
    void test(IFoo myIFoo) {
        assertNotNull(myIFoo);
        assertEquals(myIFoo.getBar(), "non-baz");
    }

}
