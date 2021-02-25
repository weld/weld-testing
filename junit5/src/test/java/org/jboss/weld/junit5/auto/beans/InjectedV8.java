package org.jboss.weld.junit5.auto.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InjectedV8 {

    @Inject
    private V8 engine;

}
