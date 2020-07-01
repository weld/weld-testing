package org.jboss.weld.junit5.auto.nested;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import javax.enterprise.context.Dependent;
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
 * Tests that outer test classes don't get instantiated again for CDI annotations.
 * <p>
 * Classes with CDI annotations are usually treated as beans by Weld and, hence, instantiated as and when required. Test classes, however, are instantiated by JUnit.
 * Weld should pick up the test instances instantiated by JUnit instead of instantiating another instance and continue to operate on them as usual with beans.
 * <br>
 * This test asserts that the same instance as the {@link org.junit.jupiter.api.Test &#064;Test} methods are invoked on
 * <ul>
 * <li>gets {@link Inject &#064;Inject}ed</li>
 * <li>and used to invoke methods
 * <ul>
 * <li>annotated with {@link Produces &#064;Produces}, or</li>
 * <li>with a parameter annotated with
 * <ul>
 * <li>{@link Observes &#064;Observes} or</li>
 * <li>{@link Disposes &#064;Disposes}</li>
 * </ul></li></ul></li></ul>
 * and assumes if that were not true that there would have been another instance of the test class.
 * <p>
 * In order to understand how the test works (or failed earlier), consider to with particular test instance each outer test class member variable would refer to
 * if Weld instantiated another, second, and undesired instance of the outer test class besides the obviously wanted one already instantiated by JUnit.
 * <p>
 * Besides all that, Weld ignores classes that don't meet valid bean requirements which affects all {@link org.junit.jupiter.api.Nested &#064;Nested} test classes due to a lack of a no-arg constructor and
 * Weld is reluctant to handle {@link Produces &#064;Produces}, {@link Observes &#064;Observes}, or {@link Disposes &#064;Disposes} annotations on them or
 * instances of {@link Inject &#064;Inject}ing nested test classes into other beans
 * but {@link Inject &#064;Inject}ing into test instances works on all test classes whether {@link org.junit.jupiter.api.Nested &#064;Nested} or not.
 * 
 * @see <a href="https://github.com/weld/weld-junit/issues/103">https://github.com/weld/weld-junit/issues/103</a>
 */
@EnableAutoWeld
public class OuterTestClassAndBeanSameInstanceTest {

    interface Bean {
        default String ping() {
            // use hashCode to verify same instance in assertSame thereby ignoring proxies
            return "ping" + Objects.hashCode(this);
        }
    }

    static void assertSameBean(Bean bean1, Bean bean2) {
        assertEquals(bean1.ping(), bean2.ping());
    }

    Bean theBean = new Bean() {
    };

    @Nested
    class NestedInjectEnclosingTestInstanceDirectlyTest {

        @Inject
        OuterTestClassAndBeanSameInstanceTest testInstanceInjected;

        @Test
        void test() {
            assertSameBean(theBean, testInstanceInjected.theBean);
        }

        @Test
        void test(OuterTestClassAndBeanSameInstanceTest outerTestInstanceAsParameter) {
            assertSameBean(theBean, outerTestInstanceAsParameter.theBean);
        }

    }

    @Dependent
    static class TestInstanceInjected {

        @Inject
        OuterTestClassAndBeanSameInstanceTest outerTestInstance;

    }

    @Nested
    class NestedInjectEnclosingTestInstanceIndirectlyTest {

        @Inject
        TestInstanceInjected testInstanceInjected;

        @Test
        void test() {
            assertSameBean(theBean, testInstanceInjected.outerTestInstance.theBean);
        }

        @Test
        void test(TestInstanceInjected testInstanceInjectedAsParameter) {
            assertSameBean(theBean, testInstanceInjectedAsParameter.outerTestInstance.theBean);
        }

    }

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
        assertSameBean(theBean, bean);
        observedBean = theBean;
    }

    @Nested
    class NestedProducesDisposesObservesTest {

        @Inject
        Bean nestedInjectBean;

        @Test
        void test() {
            assertSameBean(theBean, nestedInjectBean);
        }

        @Test
        void test(Bean nestedInjectBeanParameter) {
            assertSameBean(theBean, nestedInjectBeanParameter);
        }

        @Test
        void testEvent() {
            event.fire(nestedInjectBean);
            assertSameBean(theBean, observedBean);
        }

    }

}
