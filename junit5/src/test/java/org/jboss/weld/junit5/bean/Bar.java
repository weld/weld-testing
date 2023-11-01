package org.jboss.weld.junit5.bean;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class Bar {

    @Inject
    @Meaty
    private Foo foo;

    @Inject
    private List<String> someList;

    public List<String> getSomeList() {
        return someList;
    }

    public Foo getFoo() {
        return foo;
    }
}
