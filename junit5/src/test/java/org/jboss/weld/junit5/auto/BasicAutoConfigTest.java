package org.jboss.weld.junit5.auto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;

import org.jboss.weld.junit5.basic.Foo;
import org.jboss.weld.junit5.explicitInjection.Bar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
class BasicAutoConfigTest {

    @Inject
    private Foo foo;

    @Inject
    private Bar bar;

    @Test
    @DisplayName("Ensure the injected Foo and Bar are automatically included in container with no configuration")
    void test() {
        assertNotNull(bar);
        assertNotNull(foo);
        assertEquals(foo.getBar(), "baz");
    }

}
