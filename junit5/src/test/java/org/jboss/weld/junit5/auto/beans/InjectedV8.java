package org.jboss.weld.junit5.auto.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InjectedV8 {

    @Inject
    private V8 engine;

}
