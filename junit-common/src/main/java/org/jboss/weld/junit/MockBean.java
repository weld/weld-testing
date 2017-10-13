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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;

/**
 * This custom {@link Bean} implementation is useful for mocking.
 * <p>
 * A new instance is usually created through a {@link Builder} (see also {@link #builder()}) and then passed to the
 * {@link WeldInitiator.Builder#addBeans(Bean...)} method.
 * </p>
 *
 * @author Martin Kouba
 *
 * @param <T>
 * @see WeldInitiator.Builder#addBean(Bean)
 * @since 1.1
 */
public class MockBean<T> implements Bean<T>, PassivationCapable {

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
     * A convenient method to create a {@link Bean} with default values (see also {@link #builder()}). Additionaly, the specified bean types are added to the set
     * of bean types and {@link Bean#create(CreationalContext)} will always return the specified bean instance.
     *
     * @param beanInstance
     * @param beanTypes
     * @return a {@link MockBean} instance
     */
    public static <T> Bean<T> of(T beanInstance, Type... beanTypes) {
        return MockBean.<T> builder().types(beanTypes).creating(beanInstance).build();
    }

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private final Set<Class<? extends Annotation>> stereotypes;

    private final boolean alternative;

    private final String name;

    private final Set<Annotation> qualifiers;

    private final Set<Type> types;

    private final Class<? extends Annotation> scope;

    private final CreateFunction<T> createCallback;

    private final DestroyFunction<T> destroyCallback;

    private final String id;

    private MockBean(Set<Class<? extends Annotation>> stereotypes, boolean alternative, String name,
            Set<Annotation> qualifiers, Set<Type> types, Class<? extends Annotation> scope,
            CreateFunction<T> createCallback, DestroyFunction<T> destroyCallback) {
        this.stereotypes = stereotypes;
        this.alternative = alternative;
        this.name = name;
        this.qualifiers = qualifiers;
        this.types = types;
        this.scope = scope;
        this.createCallback = createCallback;
        this.destroyCallback = destroyCallback;
        this.id = new StringBuilder().append(MockBean.class.getName()).append("_")
                .append(SEQUENCE.incrementAndGet()).toString();
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        return createCallback.create(creationalContext);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        if (destroyCallback != null) {
            destroyCallback.destroy(instance, creationalContext);
        }
    }

    @Override
    public Class<?> getBeanClass() {
        return WeldCDIExtension.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return stereotypes;
    }

    @Override
    public boolean isAlternative() {
        return alternative;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * A builder instance should not be reused nor shared.
     *
     * @author Martin Kouba
     *
     * @param <T>
     */
    public static class Builder<T> {

        private Set<Class<? extends Annotation>> stereotypes;

        private boolean alternative;

        private String name;

        private Set<Annotation> qualifiers;

        private Set<Type> types;

        private Class<? extends Annotation> scope;

        private CreateFunction<T> createCallback;

        private DestroyFunction<T> destroyCallback;

        private Builder() {
            this.stereotypes = new HashSet<>();
            this.alternative = false;
            this.qualifiers = new HashSet<>();
            this.qualifiers.add(AnyLiteral.INSTANCE);
            this.scope = Dependent.class;
            this.types = new HashSet<>();
            this.types.add(Object.class);
        }

        /**
         *
         * @param scope
         * @return self
         * @see Bean#getScope()
         */
        public Builder<T> scope(Class<? extends Annotation> scope) {
            this.scope = scope;
            return this;
        }

        /**
         *
         * @param name
         * @return self
         * @see Bean#getName()
         */
        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        /**
         *
         * @param types
         * @return self
         * @see Bean#getTypes()
         */
        public Builder<T> types(Type... types) {
            this.types = new HashSet<>();
            Collections.addAll(this.types, types);
            return this;
        }

        /**
         *
         * @param qualifiers
         * @return self
         * @see Bean#getQualifiers()
         */
        public Builder<T> qualifiers(Annotation... qualifiers) {
            this.qualifiers = new HashSet<>();
            Collections.addAll(this.qualifiers, qualifiers);
            return this;
        }

        /**
         *
         * @param value
         * @return self
         * @see Bean#isAlternative()
         */
        public Builder<T> alternative(boolean value) {
            this.alternative = value;
            return this;
        }

        /**
         *
         * @param stereotypes
         * @return self
         * @see Bean#getStereotypes()
         */
        @SuppressWarnings("unchecked")
        public Builder<T> stereotypes(Class<? extends Annotation>... stereotypes) {
            this.stereotypes = new HashSet<>();
            Collections.addAll(this.stereotypes, stereotypes);
            return this;
        }

        /**
         * Each invocation of {@link Bean#create(CreationalContext)} will return the same instance.
         *
         * @param instance
         * @return self
         */
        public Builder<T> creating(final T instance) {
            this.createCallback = new CreateFunction<T>() {
                @Override
                public T create(CreationalContext<T> creationalContext) {
                    return instance;
                }
            };
            return this;
        }

        /**
         *
         * @param callback
         * @return self
         * @see Bean#create(CreationalContext)
         */
        public Builder<T> create(CreateFunction<T> callback) {
            this.createCallback = callback;
            return this;
        }

        /**
         *
         * @param callback
         * @return self
         * @see Bean#destroy(Object, CreationalContext)
         */
        public Builder<T> destroy(DestroyFunction<T> callback) {
            this.destroyCallback = callback;
            return this;
        }

        /**
         *
         * @return a new {@link MockBean} instance
         * @throws IllegalStateException If a create callback is not set
         */
        public MockBean<T> build() {
            if (createCallback == null) {
                throw new IllegalStateException("Create callback must not be null");
            }
            if (qualifiers.size() == 1) {
                qualifiers.add(DefaultLiteral.INSTANCE);
            }
            return new MockBean<>(stereotypes, alternative, name, qualifiers, types, scope,
                    createCallback, destroyCallback);
        }

    }

    public interface CreateFunction<T> {

        /**
         *
         * @param creationalContext
         * @return a new bean instance
         */
        T create(CreationalContext<T> creationalContext);

    }

    public interface DestroyFunction<T> {

        /**
         *
         * @param instance
         * @param creationalContext
         * @return a new bean instance
         */
        void destroy(T instance, CreationalContext<T> creationalContext);

    }

    @SuppressWarnings("all")
    static class AnyLiteral extends AnnotationLiteral<Any> implements Any {

        private static final long serialVersionUID = 1L;

        public static final Any INSTANCE = new AnyLiteral();

        private AnyLiteral() {
        }

    }

    @SuppressWarnings("all")
    static class DefaultLiteral extends AnnotationLiteral<Default> implements Default {

        public static final Default INSTANCE = new DefaultLiteral();

        private DefaultLiteral() {
        }

    }

}
