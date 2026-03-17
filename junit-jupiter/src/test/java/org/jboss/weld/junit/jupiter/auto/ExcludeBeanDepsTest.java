package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.inject.Produces;

import org.jboss.weld.junit.jupiter.basic.unsatisfied.FooDeps;
import org.jboss.weld.junit.jupiter.basic.unsatisfied.SomeFooDeps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddBeanClasses(SomeFooDeps.class)
class ExcludeBeanDepsTest {

    /**
     * FooDeps injects the Baz bean which has an unsatisfied dependency "bar-value". Excluding FooDeps should ensure
     * that its specific dependencies are not included via scanning and therefore don't need to be provided for
     * testing.
     */

    @Produces
    @ExcludeBean
    FooDeps fakeFooDeps = new FooDeps();

    @Test
    @DisplayName("Ensure @ExcludeBean excludes any specific dependencies of the excluded class")
    void test(FooDeps myFooDeps) {
        assertNotNull(myFooDeps);
    }

}
