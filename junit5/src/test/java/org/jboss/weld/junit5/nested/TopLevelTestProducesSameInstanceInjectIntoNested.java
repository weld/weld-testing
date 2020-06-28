package org.jboss.weld.junit5.nested;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
public class TopLevelTestProducesSameInstanceInjectIntoNested {

    interface Bean {
        default String ping() {
            // use hashCode to verify same instance in asserts further down ignoring proxies
            return "ping" + Objects.hashCode(this);
        }
    }

    Bean theBean = new Bean() {
    };

    @Produces
    Bean produceBean() {
        return theBean;
    }

    @Nested
    class NestedTestClassProducesMethodSameInstance {

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
    }

}
