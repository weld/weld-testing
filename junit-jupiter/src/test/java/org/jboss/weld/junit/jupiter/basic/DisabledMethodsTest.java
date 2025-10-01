package org.jboss.weld.junit.jupiter.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldJupiterExtension;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Tests that disabled methods do not trigger Weld container startup.
 * For this test to work, @TestMethodOrder is required.
 * Failure of this test might cause failures of other tests because of multiple active containers!
 *
 * @author Matej Novotny
 */
@Isolated
@ExtendWith(WeldJupiterExtension.class)
@TestMethodOrder(value = MethodOrderer.MethodName.class) // enforces ordering of methods, required!
public class DisabledMethodsTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(Foo.class);

    @BeforeEach
    public void tryUseWeldInBeforeEach() {
        assertTrue(weld.isRunning());
        assertTrue(weld.select(Foo.class).isResolvable());
    }

    @AfterEach
    public void tryUseWeldAfterEachMethod() {
        assertTrue(weld.isRunning());
        assertTrue(weld.select(Foo.class).isResolvable());
    }

    @Test
    @Disabled
    public void executedFirst() {
        // dummy disabled test method
    }

    @Test
    public void testRunningContainers() {
        // we assert that there is only one container running at this point
        assertEquals(1, WeldContainer.getRunningContainerIds().size());
    }
}
