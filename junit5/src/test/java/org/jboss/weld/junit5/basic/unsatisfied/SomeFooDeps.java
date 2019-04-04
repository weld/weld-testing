package org.jboss.weld.junit5.basic.unsatisfied;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SomeFooDeps {

    @Inject
    private FooDeps fooDeps;

}
