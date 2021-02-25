package org.jboss.weld.junit4.contexts;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@RequestScoped
public class RequestScopedProducer {

    @Produces
    @Dependent
    public String produceDependentString() {
        return "foo";
    }
}
