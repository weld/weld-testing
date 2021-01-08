package org.jboss.weld.junit5.auto;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;

@EnableAutoWeld
public class InjectedInstanceTest {

    @Dependent
    static class Foo {
    }

    @Inject
    Instance<Foo> fooInstance;

    @Test
    void test() {
        fooInstance.get();
    }

}
