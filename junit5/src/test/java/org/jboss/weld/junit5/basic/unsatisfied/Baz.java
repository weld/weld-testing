package org.jboss.weld.junit5.basic.unsatisfied;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

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
