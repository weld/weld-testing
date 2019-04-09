/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.junit;

import org.jboss.weld.environment.se.Weld;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * This custom {@link Bean} implementation is useful for mocking.
 * <p>
 * A new instance is usually created through a {@link Builder} (see also {@link #builder()}) and then passed to the
 * {@code WeldInitiator.AbstractBuilder#addBeans(Bean...)} method.
 * </p>
 *
 * @param <T>
 * @author Martin Kouba
 * @see WeldInitiator.Builder#addBean(Bean)
 * @since 1.1
 */
public class MockBean<T> extends AbstractMockBean<T> {

    private final boolean selectForSyntheticBeanArchive;

    protected MockBean(Class<?> beanClass, Set<Class<? extends Annotation>> stereotypes, boolean alternative, boolean selectForSyntheticBeanArchive, String name,
                       Set<Annotation> qualifiers, Set<Type> types, Class<? extends Annotation> scope, MockBean.CreateFunction<T> createCallback,
                       MockBean.DestroyFunction<T> destroyCallback) {
        super(beanClass, stereotypes, alternative, selectForSyntheticBeanArchive, name, qualifiers, types, scope, createCallback, destroyCallback);
        this.selectForSyntheticBeanArchive = selectForSyntheticBeanArchive;
    }

    /**
     * By default, the bean:
     * <ul>
     * <li>has no name</li>
     * <li>has {@link Dependent} scope</li>
     * <li>has {@link Any} qualifier and {@link Default} is added automatically if no other qualifiers are set</li>
     * <li>has {@link Object} bean type</li>
     * <li>has no stereotypes</li>
     * <li>is not an alternative</li>
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
        return MockBean.<T>builder().types(beanTypes).creating(beanInstance).build();
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

    /**
     * Initiator should use {@link #getBeanClass()} to select the alternative. Also the alternative should only be selected if {@link #isAlternative()} also
     * returns <code>true</code>.
     *
     * @return <code>true</code> if the initiator should select the bean for the synthetic bean archive
     */
    boolean isSelectForSyntheticBeanArchive() {
        return selectForSyntheticBeanArchive;
    }


    /**
     * A builder instance should not be reused nor shared.
     *
     * @param <T>
     * @author Martin Kouba
     * @author Matej Novotny
     */
    public static class Builder<T> extends AbstractMockBean.AbstractBuilder<T, Builder<T>> {

        private Builder() {
            super();
        }

        @Override
        protected Builder<T> self() {
            return this;
        }

        /**
         * @param value
         * @return self
         * @see Bean#isAlternative()
         */
        public Builder<T> alternative(boolean value) {
            this.alternative = value;
            return self();
        }

        /**
         * The bean is an alternative and should be automatically selected for the synthetic bean archive.
         *
         * <p>
         * Users are encouraged to specify {@link #beanClass(Class)} when using this method. The bean class is used to determine which alternative beans are
         * selected for a bean archive. By default, all mock beans share the same bean class - {@code org.jboss.weld.junit.WeldCDIExtension}.
         * </p>
         *
         * @return self
         * @see Bean#isAlternative()
         * @see Weld#addAlternative(Class)
         * @see #selectedAlternative(Class)
         */
        public Builder<T> selectedAlternative() {
            alternative(true);
            this.selectForSyntheticBeanArchive = true;
            return self();
        }

        /**
         * The bean has the given bean class, is an alternative and should be automatically selected for the synthetic bean archive.
         *
         * @param beanClass
         * @return self
         * @see #selectedAlternative()
         * @see #beanClass(Class)
         */
        public Builder<T> selectedAlternative(Class<?> beanClass) {
            selectedAlternative();
            beanClass(beanClass);
            return self();
        }


        /**
         * @return a new {@link MockBean} instance
         * @throws IllegalStateException If a create callback is not set
         */
        @Override
        public MockBean<T> build() {
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
            return new MockBean<>(beanClass, stereotypes, alternative, selectForSyntheticBeanArchive, name, qualifiers, types, scope, createCallback,
                    destroyCallback);
        }

    }

}
