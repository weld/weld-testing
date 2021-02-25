package org.jboss.weld.junit;

import jakarta.enterprise.inject.spi.Prioritized;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * A subclass of {@link MockBean} implementing {@link Prioritized} hence allowing for globally enabled alternatives.
 * Used instead of {@link MockBean} if user specified {@link MockBean.Builder#priority(int)}.
 *
 * @author Matej Novotny
 */
class MockBeanWithPriority<T> extends MockBean<T> implements Prioritized {

    private final int priority;

    MockBeanWithPriority(Class<?> beanClass, Set<Class<? extends Annotation>> stereotypes, boolean alternative,
                                 boolean selectForSyntheticBeanArchive, int priority, String name,
                                 Set<Annotation> qualifiers, Set<Type> types, Class<? extends Annotation> scope,
                                 CreateFunction<T> createCallback, DestroyFunction<T> destroyCallback) {
        super(beanClass, stereotypes, alternative, selectForSyntheticBeanArchive, name, qualifiers, types, scope, createCallback, destroyCallback);
        if (priority <= 0) {
            throw new IllegalArgumentException("MockBean cannot have priority equal or lower than 0!");
        }
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }
}
