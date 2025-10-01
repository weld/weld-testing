package org.jboss.weld.junit.jupiter.auto.beans.unsatisfied;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InjectedV8NoAnnotation {

    @Inject
    V8NoAnnotation v8;
}
