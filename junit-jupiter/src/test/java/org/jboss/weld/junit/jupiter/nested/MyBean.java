package org.jboss.weld.junit.jupiter.nested;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyBean {

    public String ping() {
        return MyBean.class.getSimpleName();
    }
}
