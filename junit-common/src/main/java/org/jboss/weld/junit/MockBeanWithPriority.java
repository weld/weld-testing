package org.jboss.weld.junit;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Prioritized;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public class MockBeanWithPriority<T> extends AbstractMockBean<T> implements Prioritized {

    private int priority;

    private MockBeanWithPriority(Class<?> beanClass, Set<Class<? extends Annotation>> stereotypes, int priority, String name,
                                 Set<Annotation> qualifiers, Set<Type> types, Class<? extends Annotation> scope, CreateFunction<T> createCallback,
                                 DestroyFunction<T> destroyCallback) {
        super(beanClass, stereotypes, true, true, name, qualifiers, types, scope, createCallback, destroyCallback);
        if (priority <= 0) {
            throw new IllegalStateException("MockBeanWithPriority must have priority >= 1!");
        }
        this.priority = priority;
    }

    /**
     * By default, the bean:
     * <ul>
     * <li>has no name</li>
     * <li>has {@link Dependent} scope</li>
     * <li>has {@link Any} qualifier and {@link Default} is added automatically if no other qualifiers are set</li>
     * <li>has {@link Object} bean type</li>
     * <li>has no stereotypes</li>
     * <li>is a globally enabled alternative</li>
     * <li>has a priority of 1</li>
     * </ul>
     *
     * <p>
     * Note that {@link Builder#creating(Object)} or {@link Builder#create(CreateFunction)} must be always set. Otherwise, an {@link IllegalStateException} is
     * thrown during {@link Builder#build()} invocation.
     * <p>
     *
     * @return a new builder instance
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * A convenient method to create a {@link Bean} with default values (see also {@link #builder()}). Additionally, the specified bean types are added to the
     * set of bean types and {@link Bean#create(CreationalContext)} will always return the specified bean instance.
     *
     * @param beanInstance
     * @param beanTypes
     * @return a {@link MockBean} instance
     */
    public static <T> Bean<T> of(T beanInstance, Type... beanTypes) {
        return MockBeanWithPriority.<T>builder().types(beanTypes).creating(beanInstance).build();
    }

    /**
     * A convenient method to create a {@link Builder} initialized from the specified bean class.
     * <p>
     * Note that the container may not be started yet and so it is not possible to use CDI SPI. Instead, we try to simulate the default bean discovery.
     * </p>
     * <p>
     * By default, {@link Unmanaged} is used to create/destroy the bean instance. However, it is possible to override this behavior.
     * </p>
     *
     * @param beanClass
     * @return a new builder instance initialized from the specified bean class
     */
    public static <T> Builder<T> read(Class<T> beanClass) {
        return readInternal(beanClass, new Builder<>()).useUnmanaged(beanClass);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * A builder instance should not be reused nor shared.
     *
     * @param <T>
     * @author Matej Novotny
     */
    public static class Builder<T> extends AbstractMockBean.AbstractBuilder<T, Builder<T>> {

        private int priority;


        private Builder() {
            super();
            this.priority = 1;
        }

        @Override
        protected Builder<T> self() {
            return this;
        }

        /**
         * @param priority
         * @return self
         * @see Prioritized#getPriority()
         */
        public Builder<T> priority(int priority) {
            this.priority = priority;
            return self();
        }


        /**
         * @return a new {@link MockBean} instance
         * @throws IllegalStateException If a create callback is not set
         */
        @Override
        public MockBeanWithPriority<T> build() {
            if (createCallback == null) {
                throw new IllegalStateException("Create callback must not be null");
            }
            if (qualifiers.size() == 1) {
                Annotation qualifier = qualifiers.iterator().next();
                if (qualifier.annotationType().equals(Named.class) || qualifier.equals(Any.Literal.INSTANCE)) {
                    // Single qualifier - @Named or @Any
                    qualifiers.add(Default.Literal.INSTANCE);
                }
            } else if (qualifiers.size() == 2 && qualifiers.contains(Any.Literal.INSTANCE)) {
                for (Annotation qualifier : qualifiers) {
                    if (qualifier.annotationType().equals(Named.class)) {
                        // Two qualifiers - @Named and @Any
                        qualifiers.add(Default.Literal.INSTANCE);
                        break;
                    }
                }
            }
            return new MockBeanWithPriority<T>(beanClass, stereotypes, priority, name, qualifiers, types, scope, createCallback,
                    destroyCallback);
        }

    }

}
