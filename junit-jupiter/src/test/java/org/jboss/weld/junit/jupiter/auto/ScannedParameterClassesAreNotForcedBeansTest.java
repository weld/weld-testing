package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.inject.Produces;

import org.jboss.weld.junit.jupiter.auto.beans.unsatisfied.ConstructedV8NoAnnotation;
import org.jboss.weld.junit.jupiter.auto.beans.unsatisfied.V8NoAnnotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddBeanClasses(ConstructedV8NoAnnotation.class)
public class ScannedParameterClassesAreNotForcedBeansTest {

    /**
     * V8NoAnnotation is *not* a "bean" class, in that it has no bean defining annotation. To satisfy
     * a dependency on it, a producer method or a reference in an @AddBeanClasses annotation
     * is required.
     * <p>
     * This test ensures that as V8NoAnnotation is discovered via class scanning it is not automatically
     * added as a bean class. If it was added that way, the bean class and producer method would
     * create an ambiguous injection case for V8NoAnnotation.
     *
     * NOTE: This case only tests for classes found from parameters (e.g. constructor injection
     * parameters)
     */

    @Produces
    private V8NoAnnotation engine = new V8NoAnnotation();

    @Test
    @DisplayName("Test that V8NoAnnotation is not ambiguous to do incorrectly being identified as a bean class from parameter")
    void test(V8NoAnnotation engine) {
        assertNotNull(engine);
    }

}
