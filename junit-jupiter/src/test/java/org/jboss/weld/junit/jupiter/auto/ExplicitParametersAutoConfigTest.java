package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.inject.Default;

import org.jboss.weld.junit.jupiter.ExplicitParamInjection;
import org.jboss.weld.junit.jupiter.basic.Foo;
import org.jboss.weld.junit.jupiter.explicitInjection.Bar;
import org.jboss.weld.junit.jupiter.explicitInjection.CustomExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@EnableAutoWeld
@ExplicitParamInjection
@ExtendWith(CustomExtension.class)
public class ExplicitParametersAutoConfigTest {

    @Test
    @DisplayName("Ensure the parameter Foo is automatically included in container and that Bar comes from the CustomExtension")
    void test(@Default Foo foo, Bar bar) {
        assertNotNull(bar);
        assertEquals(bar.ping(), CustomExtension.class.getSimpleName());
        assertNotNull(foo);
        assertEquals(foo.getBar(), "baz");
    }

}
