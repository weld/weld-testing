package org.jboss.weld.junit5.auto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jboss.weld.junit5.basic.Foo;
import org.jboss.weld.junit5.explicitInjection.Bar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
public class ParametersAutoConfigTest {

    @Test
    @DisplayName("Ensure the parameters Foo and Bar are automatically included in container with no configuration")
    void test(Foo foo, Bar bar) {
        assertNotNull(bar);
        assertEquals(Bar.class.getSimpleName(), bar.ping());
        assertNotNull(foo);
        assertEquals(foo.getBar(), "baz");
    }

}
