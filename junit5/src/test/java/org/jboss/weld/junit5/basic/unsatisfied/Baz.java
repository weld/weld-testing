package org.jboss.weld.junit5.basic.unsatisfied;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
public class Baz {

    @Inject
    @Named("bar-value")
    String bar;

    public Baz() {
    }

    public String getBar() {
        return bar;
    }

}
