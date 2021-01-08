package org.jboss.weld.junit5.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

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
