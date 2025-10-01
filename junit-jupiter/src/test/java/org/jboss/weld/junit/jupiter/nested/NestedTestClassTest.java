package org.jboss.weld.junit.jupiter.nested;

import jakarta.inject.Inject;

import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldJUnitJupiterExtension;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests a {@code @BeforeEach} method in superclass when actual test is in the inner class.
 * {@code @BeforeEach} should still be invoked for nested classes. For the below scenario to work,
 * we need to make the parent class an injection target as well.
 */
@ExtendWith(WeldJUnitJupiterExtension.class)
public class NestedTestClassTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.of(MyBean.class);

    @Inject
    MyBean myBean;

    @BeforeEach
    void setup() {
        Assertions.assertNotNull(myBean);
        myBean.ping();
    }

    @Nested
    class MyNestedTest {
        @WeldSetup
        WeldInitiator weld = WeldInitiator.of(MyBean.class);

        @Test
        void testNestedClass() {
            Assertions.assertNotNull(myBean);
            myBean.ping();
        }

        @Nested
        class TwiceNestedTest {
            @WeldSetup
            WeldInitiator weld = WeldInitiator.of(MyBean.class);

            @Test
            void testTwiceNestedClass() {
                Assertions.assertNotNull(myBean);
                myBean.ping();
            }
        }
    }
}
