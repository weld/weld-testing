package org.jboss.weld.junit5.basic;

import static org.jboss.weld.junit5.WeldAssertions.assertResolvable;
import static org.jboss.weld.junit5.WeldAssertions.assertUnresolvable;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.ofpackage.Alpha;
import org.junit.jupiter.api.Test;

public class EnableWeldOnMethodWithBeansTest {

    @Inject
    BeanManager beanManager;

    @Test
    @EnableWeld(testBeans = Foo.class)
    void testOfTestBeans() {
        assertResolvable(CDI.current().select(Foo.class));
        assertUnresolvable(CDI.current().select(Alpha.class));
        assertNotNull(beanManager);
    }

    @Test
    void weldIsNotInitialized() {
        assertNull(beanManager);
    }

}
