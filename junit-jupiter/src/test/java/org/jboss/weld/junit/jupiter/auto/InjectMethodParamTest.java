package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

@EnableAutoWeld
class InjectMethodParamTest {

    @Dependent
    static class Foo {
    }

    private Foo foo;

    @Inject
    private void setFoo(Foo foo) {
        this.foo = foo;
    }

    @Test
    void testInjectMethodParametersAreAddedToContainerWithNoConfiguration() {
        assertNotNull(foo);
    }

}
