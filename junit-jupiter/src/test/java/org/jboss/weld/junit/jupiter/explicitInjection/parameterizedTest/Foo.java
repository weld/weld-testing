package org.jboss.weld.junit.jupiter.explicitInjection.parameterizedTest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class Foo {

    @Produces
    String s = "fooString";

    String ping() {
        return Foo.class.getSimpleName();
    }
}
