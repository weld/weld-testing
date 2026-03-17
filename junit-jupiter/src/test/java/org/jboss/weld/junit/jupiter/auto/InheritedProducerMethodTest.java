package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
public class InheritedProducerMethodTest {

    @Dependent
    static class Foo {
    }

    static class BaseClass {
        @Produces
        Foo baseFooProducer() {
            return new Foo();
        }
    }

    @Nested
    class DontAddBeanClassesInheritedFromTestBaseClassTest extends BaseClass {
        @Test
        void test(BeanManager beanManager) {
            assertEquals(0, beanManager.getBeans(Foo.class).size());
        }
    }

    @Dependent
    static class SubClass extends BaseClass {
    }

    @Nested
    class DontAddBeanClassesInheritedFromBeanBaseClassTest {
        @Inject
        SubClass subClass;

        @Test
        void test(BeanManager beanManager) {
            assertEquals(0, beanManager.getBeans(Foo.class).size());
        }
    }

}
