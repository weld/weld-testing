package org.jboss.weld.junit5.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

@EnableAutoWeld
public class InjectInstanceTest {

    @Dependent
    static class Foo {
    }

    @Inject
    Instance<Foo> fooInstance;

    @Test
    void test() {
        assertNotNull(fooInstance.get());
    }

}
