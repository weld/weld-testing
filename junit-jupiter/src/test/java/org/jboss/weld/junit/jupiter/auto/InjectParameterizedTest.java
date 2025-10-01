package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

@EnableAutoWeld
public class InjectParameterizedTest {

    @Dependent
    static class Foo<T> {
    }

    @Inject
    Foo<String> foo;

    @Test
    void test() {
        assertNotNull(foo);
    }

}
