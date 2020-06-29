package org.jboss.weld.junit5.auto.nested;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests that outer test classes don't get instantiated again for {@link Produces &#064;Produces},
 * {@link Disposes &#064;Disposes}, or {@link Observes &#064;Observes} annotations.
 * 
 * In order to understand how the test works (or failed earlier),
 * consider to with particular test instance each outer test class member variable reference would refer to
 * if CDI instantiated another, second, and undesired instance of the outer test class besides the obviously wanted one instantiated by JUnit.
 */
@EnableAutoWeld
public class OuterTestClassAndBeanSameInstanceTest {

    interface Bean {
        default String ping() {
            // use hashCode to verify same instance in asserts further down thereby ignoring proxies
            return "ping" + Objects.hashCode(this);
        }
    }

    Bean theBean = new Bean() {
    };

    @Produces
    Bean produceBean() {
        return theBean;
    }

    void disposeBean(@Disposes Bean bean) {
        assertEquals(theBean.ping(), bean.ping());
    }

    @Inject @Any
    Event<Bean> event;

    Bean observedBean;

    void observeBean(@Observes Bean bean) {
        assertEquals(theBean.ping(), bean.ping());
        observedBean = theBean;
    }

    @Nested
    class NestedTest {

        @Inject
        Bean nestedInjectBean;

        @Test
        void test() {
            assertEquals(theBean.ping(), nestedInjectBean.ping());
        }

        @Test
        void test(Bean nestedInjectBeanParameter) {
            assertEquals(theBean.ping(), nestedInjectBeanParameter.ping());
        }

        @Test
        void testEvent() {
            event.fire(nestedInjectBean);
            assertEquals(theBean.ping(), observedBean.ping());
        }

    }

}
