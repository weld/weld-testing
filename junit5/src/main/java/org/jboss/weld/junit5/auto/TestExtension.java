package org.jboss.weld.junit5.auto;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;



public class TestExtension implements Extension {

  private Class<?> testClass;
  private Object testInstance;

  TestExtension(Class<?> testClass, Object testInstance) {
    this.testClass = testClass;
    this.testInstance = testInstance;
  }

  TestExtension() {
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
