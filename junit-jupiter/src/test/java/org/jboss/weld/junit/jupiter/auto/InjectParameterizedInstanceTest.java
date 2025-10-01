package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

@EnableAutoWeld
public class InjectParameterizedInstanceTest {

    @Dependent
    static class Foo<T> {
    }

    @Inject
    Instance<Foo<String>> fooInstance;

    @Test
    void test() {
        assertNotNull(fooInstance.get());
    }

}
