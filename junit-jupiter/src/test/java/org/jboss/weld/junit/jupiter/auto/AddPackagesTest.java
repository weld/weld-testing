package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;

import org.jboss.weld.junit.jupiter.auto.beans.Engine;
import org.jboss.weld.junit.jupiter.auto.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddPackages(value = Engine.class, recursively = false)
public class AddPackagesTest {

    @Inject
    private V8 engine;

    @Test
    @DisplayName("Test that @AddPackages pulls in V8 to fulfill the injected Engine interface")
    void test() {
        assertNotNull(engine);
    }

}
