package org.jboss.weld.junit5.basic;

import static org.jboss.weld.junit5.WeldAssertions.assertResolvable;
import static org.jboss.weld.junit5.WeldAssertions.assertUnresolvable;

import javax.enterprise.inject.spi.CDI;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.ofpackage.Alpha;
import org.junit.jupiter.api.Test;

@EnableWeld(testBeans = Foo.class)
public class EnableWeldOnClassWithBeansTest {

    @Test
    void testOfTestBeans() {
        assertResolvable(CDI.current().select(Foo.class));
        assertUnresolvable(CDI.current().select(Alpha.class));
    }

}
