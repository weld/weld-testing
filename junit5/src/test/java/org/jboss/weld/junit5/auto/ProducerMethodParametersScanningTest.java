package org.jboss.weld.junit5.auto;

import org.jboss.weld.junit5.auto.beans.Engine;
import org.jboss.weld.junit5.auto.beans.V6;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableAutoWeld
public class ProducerMethodParametersScanningTest {

    @Produces
    @Named("custom")
    @Dependent
    Engine getEngine(V6 v6) {
        return new Engine() {
            @Override
            public int getThrottle() {
                return v6.getThrottle();
            }

            @Override
            public void setThrottle(int value) {
                v6.setThrottle(value);
            }
        };
    }

    @Test
    public void test(@Named("custom") Engine engine) {
        assertNotNull(engine);
    }

}
