package org.jboss.weld.junit5.auto;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import java.util.Set;


/**
 * Extension class that ensure proper injection of an externally provided
 * test instance.
 *
 * NOTE: When JUnit 5 provides a test instance creation extension point this
 * should be removed and the test instance should be obtained from the
 * container via proper CDI methods.
 */
public class TestInstanceInjectionExtension implements Extension {

    private Class<?> testClass;
    private Object testInstance;

    TestInstanceInjectionExtension(Class<?> testClass, Object testInstance) {
        this.testClass = testClass;
        this.testInstance = testInstance;
    }

    TestInstanceInjectionExtension() {
    }

    <T> void rewriteTestClassInjections(@Observes ProcessInjectionTarget<T> pit) {

        if (pit.getAnnotatedType().getJavaClass().equals(testClass)) {
            InjectionTarget<T> wrapped = pit.getInjectionTarget();
            pit.setInjectionTarget(new InjectionTarget<T>() {
                @Override
                public T produce(CreationalContext<T> creationalContext) {
                    return (T) testInstance;
                }

                @Override
                public void dispose(T t) {
                    wrapped.dispose(t);
                }

                @Override
                public Set<InjectionPoint> getInjectionPoints() {
                    return wrapped.getInjectionPoints();
                }

                @Override
                public void inject(T t, CreationalContext<T> creationalContext) {
                    wrapped.inject(t, creationalContext);
                }

                @Override
                public void postConstruct(T t) {
                    wrapped.postConstruct(t);
                }

                @Override
                public void preDestroy(T t) {
                    wrapped.preDestroy(t);
                }
            });
        }
    }

}
