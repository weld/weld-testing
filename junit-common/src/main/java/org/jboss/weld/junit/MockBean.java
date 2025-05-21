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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.enterprise.inject.spi.Unmanaged;
import jakarta.enterprise.inject.spi.Unmanaged.UnmanagedInstance;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.HierarchyDiscovery;

/**
 * This custom {@link Bean} implementation is useful for mocking.
 * <p>
 * A new instance is usually created through a {@link Builder} (see also {@link #builder()}) and then passed to the
 * {@code WeldInitiator.Builder#addBeans(Bean...)} method.
 * </p>
 *
 * @author Martin Kouba
 *
 * @param <T>
 *        See also {@code WeldInitiator.Builder#addBean(Bean)} method.
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
     * Note that {@link Builder#creating(Object)} or {@link Builder#create(CreateFunction)} must be always set. Otherwise, an
     * {@link IllegalStateException} is
     * thrown during {@link Builder#build()} invocation.
     * <p>
     *
     * @return a new builder instance
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * A convenient method to create a {@link Bean} with default values (see also {@link #builder()}). Additionaly, the
     * specified bean types are added to the
     * set of bean types and {@link Bean#create(CreationalContext)} will always return the specified bean instance.
     *
     * @param beanInstance
     * @param beanTypes
     * @return a {@link MockBean} instance
     */
    public static <T> Bean<T> of(T beanInstance, Type... beanTypes) {
        return MockBean.<T> builder().types(beanTypes).creating(beanInstance).build();
    }

    /**
     * A convenient method to create a {@link Builder} initialized from the specified bean class.
     * <p>
     * Note that the container may not be started yet and so it is not possible to use CDI SPI. Instead, we try to simulate the
     * default bean discovery.
     * </p>
     * <p>
     * By default, {@link Unmanaged} is used to create/destroy the bean instance. However, it is possible to override this
     * behavior.
     * </p>
     *
     * @param beanClass
     * @return a new builder instance initialized from the specified bean class
     */
    public static <T> Builder<T> read(Class<T> beanClass) {
        return readInternal(beanClass).useUnmanaged(beanClass);
    }

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private final Set<Class<? extends Annotation>> stereotypes;

    private final boolean alternative;

    private final boolean selectForSyntheticBeanArchive;

    private final String name;

    private final Set<Annotation> qualifiers;

    private final Set<Type> types;

    private final Class<? extends Annotation> scope;

    private final CreateFunction<T> createCallback;

    private final DestroyFunction<T> destroyCallback;

    private final String id;

    private final Class<?> beanClass;

    protected MockBean(Class<?> beanClass, Set<Class<? extends Annotation>> stereotypes, boolean alternative,
            boolean selectForSyntheticBeanArchive, String name,
            Set<Annotation> qualifiers, Set<Type> types, Class<? extends Annotation> scope, CreateFunction<T> createCallback,
            DestroyFunction<T> destroyCallback) {
        this.beanClass = beanClass;
        this.stereotypes = stereotypes;
        this.alternative = alternative;
        this.selectForSyntheticBeanArchive = selectForSyntheticBeanArchive;
        this.name = name;
        this.qualifiers = qualifiers;
        this.types = types;
        this.scope = scope;
        this.createCallback = createCallback;
        this.destroyCallback = destroyCallback;
        this.id = new StringBuilder().append(MockBean.class.getName()).append("_").append(SEQUENCE.incrementAndGet())
                .toString();
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
        return beanClass;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Set<Type> getTypes() {
        return ImmutableSet.copyOf(types);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return ImmutableSet.copyOf(qualifiers);
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
        return ImmutableSet.copyOf(stereotypes);
    }

    @Override
    public boolean isAlternative() {
        return alternative;
    }

    /**
     * Initiator should use {@link #getBeanClass()} to select the alternative. Also the alternative should only be selected if
     * {@link #isAlternative()} also
     * returns <code>true</code>.
     *
     * @return <code>true</code> if the initiator should select the bean for the synthetic bean archive
     */
    boolean isSelectForSyntheticBeanArchive() {
        return selectForSyntheticBeanArchive;
    }

    @Override
    public String getId() {
        return id;
    }

    private static <T> Builder<T> readInternal(Class<T> beanClass) {
        // Note that we cannot use BeanManager here as the container may not be started yet
        Builder<T> builder = new Builder<T>().beanClass(beanClass);

        // Find all stereotypes
        Set<Annotation> stereotypes = getStereotypes(beanClass);
        // Name
        Named named = beanClass.getAnnotation(Named.class);
        if (named != null) {
            if ("".equals(named.value())) {
                builder.name(getDefaultName(beanClass));
            } else {
                builder.name(named.value());
            }
        } else {
            for (Annotation stereotype : stereotypes) {
                if (stereotype.annotationType().isAnnotationPresent(Named.class)) {
                    builder.name(getDefaultName(beanClass));
                    break;
                }
            }
        }
        // Scope
        Set<Annotation> scopes = getScopes(beanClass);
        if (scopes.isEmpty()) {
            for (Annotation stereotype : stereotypes) {
                scopes.addAll(getScopes(stereotype.annotationType()));
            }
        }
        if (!scopes.isEmpty()) {
            if (scopes.size() > 1) {
                throw new IllegalStateException(
                        "At most one scope may be specifie [beanClass: " + beanClass + ", scopes: " + scopes + "]");
            }
            builder.scope(scopes.iterator().next().annotationType());
        }
        // Types
        builder.types(new HierarchyDiscovery(beanClass).getTypeClosure());
        // Qualifiers
        for (Annotation annotation : beanClass.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                builder.addQualifier(annotation);
            }
        }
        // Alternative
        if (beanClass.isAnnotationPresent(Alternative.class)) {
            builder.alternative(true);
        } else {
            for (Annotation stereotype : stereotypes) {
                if (stereotype.annotationType().isAnnotationPresent(Alternative.class)) {
                    builder.alternative(true);
                    break;
                }
            }
        }
        return builder;
    }

    private static Set<Annotation> getStereotypes(AnnotatedElement element) {
        Set<Annotation> stereotypes = new HashSet<>();
        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Stereotype.class)) {
                stereotypes.add(annotation);
                stereotypes.addAll(getStereotypes(annotation.annotationType()));
            }
        }
        return stereotypes;
    }

    private static Set<Annotation> getScopes(AnnotatedElement element) {
        Set<Annotation> scopes = new HashSet<>();
        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Scope.class)
                    || annotation.annotationType().isAnnotationPresent(NormalScope.class)) {
                scopes.add(annotation);
            }
        }
        return scopes;
    }

    private static String getDefaultName(Class<?> beanClass) {
        StringBuilder defaultName = new StringBuilder(beanClass.getSimpleName());
        defaultName.setCharAt(0, Character.toLowerCase(beanClass.getSimpleName().charAt(0)));
        return defaultName.toString();
    }

    /**
     * A builder instance should not be reused nor shared.
     *
     * @author Martin Kouba
     *
     * @param <T>
     */
    public static class Builder<T> {

        private Class<?> beanClass;

        private Set<Class<? extends Annotation>> stereotypes;

        private boolean alternative;

        private boolean selectForSyntheticBeanArchive;

        private Integer priority;

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
            this.beanClass = WeldCDIExtension.class;
            this.priority = null;
        }

        /**
         *
         * @param beanClass
         * @return self
         * @see Bean#getBeanClass()
         */
        public Builder<T> beanClass(Class<?> beanClass) {
            this.beanClass = beanClass;
            return this;
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
            this.types.clear();
            Collections.addAll(this.types, types);
            return this;
        }

        /**
         *
         * @param types
         * @return self
         */
        public Builder<T> types(Set<Type> types) {
            this.types.clear();
            this.types.addAll(types);
            return this;
        }

        /**
         *
         * @param type
         * @return self
         */
        public Builder<T> addType(Type type) {
            this.types.add(type);
            return this;
        }

        /**
         *
         * @param qualifiers
         * @return self
         * @see Bean#getQualifiers()
         */
        public Builder<T> qualifiers(Annotation... qualifiers) {
            this.qualifiers.clear();
            Collections.addAll(this.qualifiers, qualifiers);
            return this;
        }

        /**
         *
         * @param qualifier
         * @return self
         */
        public Builder<T> addQualifier(Annotation qualifier) {
            this.qualifiers.add(qualifier);
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
         * Programmatic equivalent to to putting {@link jakarta.annotation.Priority} annotation on a bean class.
         * Allows for globally enabled alternatives.
         *
         * @param priority
         * @return self
         * @see Prioritized#getPriority()
         */
        public Builder<T> priority(int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * This beans is a globally enabled alternative with a priority equal to its method parameter.
         * Calling this method is a shortcut for {@link Builder#priority(int)} and {@link Builder#alternative(boolean)}
         *
         * @param priority
         * @return self
         */
        public Builder<T> globallySelectedAlternative(int priority) {
            this.priority = priority;
            this.alternative = true;
            return this;
        }

        /**
         * The bean is an alternative and should be automatically selected for the synthetic bean archive.
         *
         * <p>
         * Users are encouraged to specify {@link #beanClass(Class)} when using this method. The bean class is used to determine
         * which alternative beans are
         * selected for a bean archive. By default, all mock beans share the same bean class -
         * {@code org.jboss.weld.junit.WeldCDIExtension}.
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
            return this;
        }

        /**
         * The bean has the given bean class, is an alternative and should be automatically selected for the synthetic bean
         * archive.
         *
         * @param beanClass
         * @return self
         * @see #selectedAlternative()
         * @see #beanClass(Class)
         */
        public Builder<T> selectedAlternative(Class<?> beanClass) {
            selectedAlternative();
            beanClass(beanClass);
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
            this.stereotypes.clear();
            Collections.addAll(this.stereotypes, stereotypes);
            return this;
        }

        /**
         *
         * @param stereotype
         * @return self
         */
        public Builder<T> addStereotype(Class<? extends Annotation> stereotype) {
            this.stereotypes.add(stereotype);
            return this;
        }

        /**
         * Each invocation of {@link Bean#create(CreationalContext)} will return the same instance.
         *
         * @param instance
         * @return self
         */
        public Builder<T> creating(final T instance) {
            this.createCallback = ctx -> instance;
            return this;
        }

        /**
         * Use {@link Unmanaged} to create/destroy the bean instance.
         *
         * <p>
         * NOTE: {@code CreationalContext#toString()} is used as a key in a map and therefore must be unique for the lifetime of
         * a bean instance. Weld
         * implementation fulfills this requirement.
         * </p>
         *
         * @return self
         * @see UnmanagedInstance
         */
        @SuppressWarnings("unchecked")
        public Builder<T> useUnmanaged(Class<T> beanClass) {
            Map<String, UnmanagedInstance<?>> ctxToUnmanaged = new ConcurrentHashMap<>();
            create(ctx -> {
                Unmanaged<?> unmanaged = new Unmanaged<>(WeldContainer.current().getBeanManager(), beanClass);
                UnmanagedInstance<?> unmanagedInstance = unmanaged.newInstance();
                ctxToUnmanaged.put(ctx.toString(), unmanagedInstance);
                return (T) unmanagedInstance.produce().inject().postConstruct().get();
            });
            destroy((o, ctx) -> {
                UnmanagedInstance<?> unmanagedInstance = ctxToUnmanaged.remove(ctx.toString());
                if (unmanagedInstance != null) {
                    if (!unmanagedInstance.get().equals(o)) {
                        throw new IllegalStateException(
                                "Unmanaged instance [" + unmanagedInstance.get()
                                        + "] is not equal to the bean instance to be destroyed: " + o);
                    }
                    unmanagedInstance.preDestroy().dispose();
                }
            });
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
            Set<Annotation> normalizedQualfiers = new HashSet<Annotation>(qualifiers);
            normalizedQualfiers.remove(Any.Literal.INSTANCE);
            normalizedQualfiers.remove(Default.Literal.INSTANCE);
            if (normalizedQualfiers.isEmpty()) {
                normalizedQualfiers.add(Default.Literal.INSTANCE);
                normalizedQualfiers.add(Any.Literal.INSTANCE);
            } else {
                ImmutableSet.Builder<Annotation> builder = ImmutableSet.builder();
                if (normalizedQualfiers.size() == 1
                        && normalizedQualfiers.iterator().next().annotationType().equals(Named.class)) {
                    builder.add(Default.Literal.INSTANCE);
                }
                builder.add(Any.Literal.INSTANCE);
                builder.addAll(qualifiers);
                normalizedQualfiers = builder.build();
            }
            // if given any priority, we will instead initialize MockBeanWithPriority
            if (priority != null) {
                return new MockBeanWithPriority<>(beanClass, stereotypes, alternative, selectForSyntheticBeanArchive, priority,
                        name, normalizedQualfiers, types, scope, createCallback,
                        destroyCallback);
            } else {
                return new MockBean<>(beanClass, stereotypes, alternative, selectForSyntheticBeanArchive, name,
                        normalizedQualfiers, types, scope, createCallback,
                        destroyCallback);
            }
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
         */
        void destroy(T instance, CreationalContext<T> creationalContext);

    }

    @SuppressWarnings("all")
    static class AnyLiteral extends AnnotationLiteral<Any> implements Any {

        private static final long serialVersionUID = -1366513826361712883L;

        public static final Any INSTANCE = new AnyLiteral();

        private AnyLiteral() {
        }

    }

    @SuppressWarnings("all")
    static class DefaultLiteral extends AnnotationLiteral<Default> implements Default {

        private static final long serialVersionUID = -1395539980812895226L;

        public static final Default INSTANCE = new DefaultLiteral();

        private DefaultLiteral() {
        }

    }

}
