package org.jboss.weld.junit5.auto;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.injection.ForwardingInjectionTarget;

class TestInstanceInjectionTarget<T> extends ForwardingInjectionTarget<T> {

    InjectionTarget<T> wrapped;
    T testInstance;

    TestInstanceInjectionTarget(InjectionTarget<T> wrapped, T testInstance) {
        this.wrapped = wrapped;
        this.testInstance = testInstance;
    }

    @Override
    protected InjectionTarget<T> delegate() {
        return wrapped;
    }

    @Override
    public T produce(CreationalContext<T> creationalContext) {
        return testInstance;
    }

}
