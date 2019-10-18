package org.jboss.weld.junit5.nested;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyBean {

    public String ping() {
        return MyBean.class.getSimpleName();
    }
}
