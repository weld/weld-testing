package org.jboss.weld.junit.jupiter.basic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SomeIFoo {

    @Inject
    private IFoo foo;

}
