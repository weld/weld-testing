package org.jboss.weld.junit5.auto.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ConstructedV8 {

    private V8 engine;

    @Inject
    public ConstructedV8(V8 engine) {
        this.engine = engine;
    }

}
