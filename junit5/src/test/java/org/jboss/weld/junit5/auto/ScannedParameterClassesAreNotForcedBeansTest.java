package org.jboss.weld.junit5.auto;


import org.jboss.weld.junit5.auto.beans.ConstructedV8;
import org.jboss.weld.junit5.auto.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@EnableAutoWeld
@AddBeanClasses(ConstructedV8.class)
public class ScannedParameterClassesAreNotForcedBeansTest {

    /**
     * V8 is *not* a "bean" class, in that it has no bean defining annotation. To satisfy
     * a dependency on it, a producer method or a reference in an @AddBeanClasses annotation
     * is required.
     * <p>
     * This test ensures that as V8 is discovered via class scanning it is not automatically
     * added as a bean class. If it was added that way, the bean class and producer method would
     * create an ambiguous injection case for V8.
     *
     * NOTE: This case only tests for classes found from parameters (e.g. constructor injection
     * parameters)
     */

    @Produces
    private V8 engine = new V8();

    @Test
    @DisplayName("Test that V8 is not ambiguous to do incorrectly being identified as a bean class from parameter")
    void test(V8 engine) {
        assertNotNull(engine);
    }

}
