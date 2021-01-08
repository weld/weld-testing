package org.jboss.weld.junit5.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
public class InheritedInjectedTest {

    @Dependent
    static class Foo {
    }

    static class BaseClass {
        @Inject
        Foo foo;
    }

    @Nested
    class InjectIntoInheritedFieldsFromTestBaseClassTest extends BaseClass {

        @Test
        @DisplayName("Test injected fields a test class inherits from a base class")
        void test() {
            assertNotNull(foo);
        }
    }

    @Dependent
    static class SubClass extends BaseClass {
    }

    @Nested
    class InjectIntoInheritedFieldsFromBeanBaseClassTest {

        @Inject
        SubClass inheritsInjected;

        /**
         * Injection of {@link #inheritsInjected} requires {@link InheritedInjectedTest.Foo} having been identified and added as a bean class to Weld.
         */
        @Test
        @DisplayName("Test inherited injected fields")
        void test() {
            assertNotNull(inheritsInjected.foo);
        }
    }

}
