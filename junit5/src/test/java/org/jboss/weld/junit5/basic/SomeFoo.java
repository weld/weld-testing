package org.jboss.weld.junit5.basic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SomeFoo {

    @Inject
    private Foo foo;

}
