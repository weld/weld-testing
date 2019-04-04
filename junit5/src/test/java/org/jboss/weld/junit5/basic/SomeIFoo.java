package org.jboss.weld.junit5.basic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SomeIFoo {

    @Inject
    private IFoo foo;

}
