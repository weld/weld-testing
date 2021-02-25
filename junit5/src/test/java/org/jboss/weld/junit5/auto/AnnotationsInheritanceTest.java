package org.jboss.weld.junit5.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.jboss.weld.junit5.auto.beans.Engine;
import org.jboss.weld.junit5.auto.beans.V8;
import org.jboss.weld.junit5.auto.extension.AddedExtension;
import org.jboss.weld.junit5.auto.interceptorAndDecorator.DecoratedBean;
import org.jboss.weld.junit5.auto.interceptorAndDecorator.InterceptedBean;
import org.jboss.weld.junit5.auto.interceptorAndDecorator.TestDecorator;
import org.jboss.weld.junit5.auto.interceptorAndDecorator.TestInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests the inheritance of "Weld-JUnit" annotations from test parent classes.
 */
public class AnnotationsInheritanceTest {

    @EnableAutoWeld
    @AddBeanClasses(V8.class)
    class BaseAddBeanClassesTest {
    }

    @Nested
    class AddBeanClassesTest extends BaseAddBeanClassesTest {
        @Inject
        private Engine engine;

        @Test
        @DisplayName("Test that @AddBeanClasses pulls in V8 to fulfill the injected Engine interface")
        void test() {
            assertNotNull(engine);
        }
    }

    @EnableAutoWeld
    @AddEnabledDecorators(TestDecorator.class)
    class BaseAddDecoratorTest {
    }

    @Nested
    class AddDecoratorTest extends BaseAddDecoratorTest {
        @Inject
        DecoratedBean bean;

        @Test
        public void testBeanIsDecorated() {
            Assertions.assertNotNull(bean);
            Assertions.assertEquals(TestDecorator.class.toString() + DecoratedBean.class.toString(), bean.ping());
        }
    }

    @EnableAutoWeld
    @AddExtensions(AddedExtension.class)
    class BaseAddExtensionsTest {
    }

    @Nested
    class AddExtensionsTest extends BaseAddExtensionsTest {
        @Test
        @DisplayName("Test that @AddExtensions adds the specified extensions")
        void test() {
            assertTrue(AddedExtension.isEnabled());
        }
    }

    @EnableAutoWeld
    @AddEnabledInterceptors(TestInterceptor.class)
    class BaseAddInterceptorTest {
    }

    @Nested
    class AddInterceptorTest extends BaseAddInterceptorTest {
        @Inject
        InterceptedBean bean;

        @Test
        public void testBeanIsIntercepted() {
            Assertions.assertNotNull(bean);
            Assertions.assertEquals(TestInterceptor.class.toString() + InterceptedBean.class.toString(), bean.ping());
        }
    }

    @EnableAutoWeld
    @AddPackages(Engine.class)
    class BaseAddPackagesTest {
    }

    @Nested
    class AddPackagesTest extends BaseAddPackagesTest {
        @Inject
        private V8 engine;

        @Test
        @DisplayName("Test that @AddPackages pulls in V8 (without bean defining annotation) to fulfill the injected Engine interface")
        void test() {
            assertNotNull(engine);
        }
    }

}
