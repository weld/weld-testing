package org.jboss.weld.junit.jupiter.basic.unsatisfied;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SomeFooDeps {

    @Inject
    private FooDeps fooDeps;

}
