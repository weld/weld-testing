package org.jboss.weld.junit5.contexts;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

@RequestScoped
public class RequestScopedProducer {

    @Produces
    @Dependent
    public String produceDependentString() {
        return "foo";
    }
}
