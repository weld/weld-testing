package org.jboss.weld.junit5.initiator.discovery;

import jakarta.inject.Inject;

import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.junit5.initiator.bean.Bar;
import org.jboss.weld.junit5.initiator.bean.Foo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@EnableWeld
public class NestedClassesWeldInitiatorTest {

    @WeldSetup
    private WeldInitiator outerWeld = WeldInitiator.of(Foo.class);

    @Inject
    Foo outerFoo;

    @Test
    void testWeldWorksInOuterClass() {
        Assertions.assertNotNull(outerFoo);
        Assertions.assertThrows(UnsatisfiedResolutionException.class, () -> outerWeld.select(Bar.class).get());
    }

    @Nested
    class NestedWithoutInitiatorTest {

        @Inject
        Foo innerFoo;

        @Test
        void testWeldWorksInInnerClass() {
            Assertions.assertNotNull(outerFoo);
            Assertions.assertNotNull(innerFoo);
            Assertions.assertThrows(UnsatisfiedResolutionException.class, () -> outerWeld.select(Bar.class).get());
        }

        @Nested
        class TwiceNestedWithoutInitiatorTest {

            @Inject
            Foo innerInnerFoo;

            @Test
            void testWeldWorksInSecondInnerClass() {
                Assertions.assertNotNull(outerFoo);
                Assertions.assertNotNull(innerFoo);
                Assertions.assertNotNull(innerInnerFoo);
                Assertions.assertThrows(UnsatisfiedResolutionException.class, () -> outerWeld.select(Bar.class).get());
            }
        }
    }

    @Nested
    class NestedWithInitiatorTest {

        @WeldSetup
        private WeldInitiator innerWeld = WeldInitiator.of(Foo.class, Bar.class);

        @Inject
        Foo innerFoo;

        @Inject
        Bar bar;

        @Test
        void testWeldWorksInInnerClass() {
            Assertions.assertNotNull(outerFoo);
            Assertions.assertNotNull(bar);

            // verify outer weld container is not running
            Assertions.assertThrows(IllegalStateException.class, () -> outerWeld.select(Foo.class));
        }

        @Nested
        class TwiceNestedWithoutInitiatorTest {

            @Inject
            Foo innerInnerFoo;

            @Inject
            Bar innerBar;

            @Test
            void testWeldWorksInSecondInnerClass() {
                Assertions.assertNotNull(outerFoo);
                Assertions.assertNotNull(innerFoo);
                Assertions.assertNotNull(innerInnerFoo);

                Assertions.assertNotNull(bar);
                Assertions.assertNotNull(innerBar);

                // verify outer weld container is not running
                Assertions.assertThrows(IllegalStateException.class, () -> outerWeld.select(Foo.class));
            }
        }
    }

    @Nested
    class NestedWithInheritedWeldInitiatorTest extends SuperclassWithProtectedWeldInitiator {

        @Inject
        Foo innerFoo;

        @Test
        void testInheritedTakesPrecedenceOverEnclosingWeldInitiator() {
            Assertions.assertNotNull(outerFoo);
            Assertions.assertNotNull(innerFoo);
            Assertions.assertThrows(UnsatisfiedResolutionException.class, () -> weld.select(Bar.class).get());

            // verify outer weld container is not running
            Assertions.assertThrows(IllegalStateException.class, () -> outerWeld.select(Foo.class));
        }
    }
}
