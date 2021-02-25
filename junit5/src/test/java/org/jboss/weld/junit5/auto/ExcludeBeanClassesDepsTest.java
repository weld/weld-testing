package org.jboss.weld.junit5.auto;

import org.jboss.weld.junit5.basic.unsatisfied.FooDeps;
import org.jboss.weld.junit5.basic.unsatisfied.SomeFooDeps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.Produces;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@EnableAutoWeld
@AddBeanClasses(SomeFooDeps.class)
@ExcludeBeanClasses(FooDeps.class)
class ExcludeBeanClassesDepsTest {

    /**
     * FooDeps injects the Baz bean which has an unsatisfied dependency "bar-value". Excluding FooDeps should ensure
     * that its specific dependencies are not included via scanning and therefore don't need to be provided for
     * testing.
     */

    @Produces
    FooDeps fakeFooDeps = new FooDeps();

    @Test
    @DisplayName("Ensure @ExcludeBeanClasses excludes any specific dependencies of the excluded classes")
    void test(FooDeps myFooDeps) {
        assertNotNull(myFooDeps);
    }

}
