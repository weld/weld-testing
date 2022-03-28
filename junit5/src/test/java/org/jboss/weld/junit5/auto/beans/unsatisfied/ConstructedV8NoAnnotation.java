package org.jboss.weld.junit5.auto.beans.unsatisfied;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ConstructedV8NoAnnotation {

    private V8NoAnnotation engine;

    @Inject
    public ConstructedV8NoAnnotation(V8NoAnnotation engine) {
        this.engine = engine;
    }

}
