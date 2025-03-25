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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.ContainerInstance;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class AbstractWeldInitiator implements Instance<Object>, ContainerInstance {

    /**
     * The returned {@link Weld} instance has:
     * <ul>
     * <li>automatic discovery disabled</li>
     * <li>concurrent deployment disabled</li>
     * </ul>
     * <p>
     * This method is not meant to be invoked directly!
     * Use the {@code WeldInitiator#createWeld()} of the given impl (JUnit 4/5, Spock).
     *
     * @return a new {@link Weld} instance suitable for testing
     */
    public static Weld createDefaultWeld() {
        return new Weld().disableDiscovery().property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false);
    }

    protected final Weld weld;

    protected final List<ToInject> instancesToInject;

    protected final Set<Class<? extends Annotation>> scopesToActivate;

    protected final Set<Bean<?>> beans;

    protected final WeldCDIExtension extension;

    private final Map<String, Object> resources;

    private final Function<InjectionPoint, Object> ejbFactory;

    private final Function<InjectionPoint, Object> persistenceUnitFactory;

    private final Function<InjectionPoint, Object> persistenceContextFactory;

    protected volatile WeldContainer container;

    protected AbstractWeldInitiator(Weld weld, List<Object> instancesToInject,
            Set<Class<? extends Annotation>> scopesToActivate, Set<Bean<?>> beans,
            Map<String, Object> resources, Function<InjectionPoint, Object> ejbFactory,
            Function<InjectionPoint, Object> persistenceUnitFactory,
            Function<InjectionPoint, Object> persistenceContextFactory) {
        this.instancesToInject = new ArrayList<>();
        for (Object instance : instancesToInject) {
            this.instancesToInject.add(createToInject(instance));
        }
        this.scopesToActivate = scopesToActivate;
        this.beans = beans;
        this.weld = weld;
        boolean hasMockInterceptor = false;
        boolean dummyBeanAdded = false;
        if (hasScopesToActivate() || hasBeansToAdd()) {
            this.extension = new WeldCDIExtension(this.scopesToActivate, this.beans);
            for (Bean<?> bean : this.beans) {
                if (bean instanceof MockBean) {
                    MockBean<?> mockBean = (MockBean<?>) bean;
                    if (mockBean.isAlternative() && mockBean.isSelectForSyntheticBeanArchive()) {
                        this.weld.addAlternative(mockBean.getBeanClass());
                        if (!dummyBeanAdded) {
                            // by adding a dummy bean we make sure that synthetic archive gets created even if
                            // the enabled alternative was the only bean added
                            this.weld.addBeanClass(Object.class);
                            dummyBeanAdded = true;
                        }
                    }
                } else if (bean instanceof MockInterceptor && ((MockInterceptor) bean).hasDefaultBeanClass()) {
                    hasMockInterceptor = true;
                }
            }
            // Automatically enable all mock interceptors for the synthetic bean archive
            if (hasMockInterceptor) {
                this.weld.addInterceptor(MockInterceptor.class);
            }
            this.weld.addExtension(this.extension);
        } else {
            this.extension = null;
        }
        this.resources = resources;
        this.ejbFactory = ejbFactory;
        this.persistenceContextFactory = persistenceContextFactory;
        this.persistenceUnitFactory = persistenceUnitFactory;
    }

    protected ToInject createToInject(Object instanceToInject) {
        return new ToInject(instanceToInject);
    }

    /**
     * Injects the given non-contextual instance immediately. The returned {@link AutoCloseable} should be used
     * to release the creational context once the injected beans are no longer needed.
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * try (AutoCloseable contextReleaser = injectNonContextual(this)) {
     *     // do some things with the injected instances
     * }
     * }</pre>
     *
     * @param target the target to inject
     * @return an {@code AutoCloseable} to release the creational context
     */
    public AutoCloseable injectNonContextual(Object target) {
        ToInject toInject = new ToInject(target);
        toInject.inject();
        return toInject::release;
    }

    @Override
    public Iterator<Object> iterator() {
        checkContainer();
        return container.iterator();
    }

    @Override
    public Object get() {
        checkContainer();
        return container.get();
    }

    @Override
    public WeldInstance<Object> select(Annotation... qualifiers) {
        checkContainer();
        return container.select(qualifiers);
    }

    @Override
    public <U> WeldInstance<U> select(Class<U> subtype, Annotation... qualifiers) {
        checkContainer();
        return container.select(subtype, qualifiers);
    }

    @Override
    public <U> WeldInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        checkContainer();
        return container.select(subtype, qualifiers);
    }

    @Override
    public boolean isUnsatisfied() {
        checkContainer();
        return container.isUnsatisfied();
    }

    @Override
    public boolean isAmbiguous() {
        checkContainer();
        return container.isAmbiguous();
    }

    @Override
    public void destroy(Object instance) {
        checkContainer();
        container.destroy(instance);
    }

    @Override
    public Handle<Object> getHandle() {
        checkContainer();
        return container.getHandle();
    }

    @Override
    public Iterable<? extends Handle<Object>> handles() {
        checkContainer();
        return container.handles();
    }

    /**
     * Allows to fire events.
     *
     * @return an event object
     */
    @SuppressWarnings("unchecked")
    public Event<Object> event() {
        checkContainer();
        try {
            // We need to use reflection due to some compatibility issues
            Method eventMethod = container.getClass().getMethod("event");
            return (Event<Object>) eventMethod.invoke(container);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalStateException("Cannot invoke WeldContainer.event() method using reflection", e);
        }
    }

    @Override
    public BeanManager getBeanManager() {
        checkContainer();
        return container.getBeanManager();
    }

    @Override
    public String getId() {
        return container.getId();
    }

    /**
     * Note that any container-based operation will result in {@link IllegalStateException} after shutdown.
     */
    @Override
    public void shutdown() {
        container.shutdown();
    }

    /**
     *
     * @return <code>true</code> if the container was initialized completely and is not shut down yet, <code>false</code>
     *         otherwise
     */
    public boolean isRunning() {
        return (container != null) && container.isRunning();
    }

    /**
     * This method should be used when a Weld-specific API is needed.
     *
     * @return the underlying container instance
     */
    public WeldContainer container() {
        checkContainer();
        return container;
    }

    private void checkContainer() {
        if (container == null || !container.isRunning()) {
            throw new IllegalStateException("Weld container is not running");
        }
    }

    protected void injectInstances() {
        if (instancesToInject != null) {
            for (ToInject toInject : instancesToInject) {
                toInject.inject();
            }
        }
    }

    protected void releaseInstances() {
        if (instancesToInject != null) {
            for (ToInject toInject : instancesToInject) {
                toInject.release();
            }
        }
    }

    private boolean hasScopesToActivate() {
        return scopesToActivate != null && !scopesToActivate.isEmpty();
    }

    private boolean hasBeansToAdd() {
        return beans != null && !beans.isEmpty();
    }

    protected class ToInject {

        private final Object instance;

        private volatile CreationalContext<?> creationalContext;

        ToInject(Object instance) {
            this.instance = instance;
        }

        void inject() {
            BeanManager beanManager = container.getBeanManager();
            CreationalContext<Object> ctx = beanManager.createCreationalContext(null);
            @SuppressWarnings("unchecked")
            InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) beanManager
                    .getInjectionTargetFactory(beanManager.createAnnotatedType(instance.getClass()))
                    .createInjectionTarget(null);
            injectionTarget.inject(instance, ctx);
            creationalContext = ctx;
        }

        void release() {
            if (creationalContext != null) {
                creationalContext.release();
            }
        }

    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Weld object reference is intentionally externally mutable")
    protected static abstract class AbstractBuilder<I extends AbstractWeldInitiator, T extends AbstractBuilder<I, T>> {

        protected final Weld weld;

        protected final List<Object> instancesToInject;

        protected final Set<Class<? extends Annotation>> scopesToActivate;

        protected final Set<Bean<?>> beans;

        protected final Map<String, Object> resources;

        private Function<InjectionPoint, Object> ejbFactory;

        private Function<InjectionPoint, Object> persistenceUnitFactory;

        private Function<InjectionPoint, Object> persistenceContextFactory;

        public AbstractBuilder(Weld weld) {
            this.weld = weld;
            this.instancesToInject = new ArrayList<>();
            this.scopesToActivate = new HashSet<>();
            this.beans = new HashSet<>();
            this.resources = new HashMap<>();
        }

        /**
         * Activate and deactivate contexts for the given normal scopes for the lifetime of the initialized Weld container, by
         * default for each test method
         * execution.
         * <p>
         * {@link ApplicationScoped} is ignored as it is always active.
         * </p>
         *
         * @param normalScopes
         * @return self
         */
        @SafeVarargs
        public final T activate(Class<? extends Annotation>... normalScopes) {
            for (Class<? extends Annotation> scope : normalScopes) {
                if (ApplicationScoped.class.equals(scope)) {
                    continue;
                }
                if (!scope.isAnnotationPresent(NormalScope.class)) {
                    throw new IllegalArgumentException("Only annotations annotated with @NormalScope are supported!");
                }
                this.scopesToActivate.add(scope);
            }
            return self();
        }

        protected Function<InjectionPoint, Object> getEjbFactory() {
            return ejbFactory;
        }

        protected Function<InjectionPoint, Object> getPersistenceContextFactory() {
            return persistenceContextFactory;
        }

        protected Function<InjectionPoint, Object> getPersistenceUnitFactory() {
            return persistenceUnitFactory;
        }

        /**
         * Instructs the initiator to inject the given non-contextual instance once the container is started, i.e. during test
         * execution.
         *
         * <p>
         * This method could be used e.g. to inject a test class instance:
         * </p>
         *
         * <pre>
         * public class InjectTest {
         *
         *     &#64;Rule
         *     public WeldInitiator weld = WeldInitiator.fromTestPackage().inject(this).build();
         *
         *     &#64;Inject
         *     Foo foo;
         *
         *     &#64;Test
         *     public void testFoo() {
         *         assertEquals("foo", foo.getId());
         *     }
         * }
         * </pre>
         *
         * <p>
         * Injected {@link Dependent} bean instances are destroyed after the test execution. However, the lifecycle of the
         * non-contextual instance is not
         * managed by the container and all injected references will be invalid after the test execution.
         * </p>
         *
         * @param instance
         * @return self
         */
        public T inject(Object instance) {
            this.instancesToInject.add(instance);
            return self();
        }

        /**
         * Instructs the initiator to add the specified beans during {@link AfterBeanDiscovery} notification.
         *
         * @param beans
         * @return self
         * @see AfterBeanDiscovery#addBean(Bean)
         * @see MockBean
         * @see MockInterceptor
         * @since 1.1
         */
        public T addBeans(Bean<?>... beans) {
            Collections.addAll(this.beans, beans);
            return self();
        }

        /**
         * Binds a name to an object. This allows to mock {@link Resource} injection points easily, e.g.:
         *
         * <pre>
         * &#64;Dependent
         * class Foo {
         *
         *     &#64;Resource(lookup = "bar")
         *     String bar;
         * }
         * </pre>
         *
         * @param name
         * @param resource
         * @return self
         * @since 1.2
         */
        public T bindResource(String name, Object resource) {
            resources.put(name, resource);
            return self();
        }

        /**
         * Makes it possible to mock {@code @EJB} injection points.
         *
         * <p>
         * Note that for Weld 3 {@code org.jboss.weld.module:weld-ejb} dependency is also required.
         * </p>
         *
         * @param ejbFactory
         * @return self
         * @since 1.2
         */
        public T setEjbFactory(Function<InjectionPoint, Object> ejbFactory) {
            this.ejbFactory = ejbFactory;
            return self();
        }

        /**
         * Makes it possible to mock {@code PersistenceUnit} injection points.
         *
         * @param persistenceUnitFactory
         * @return self
         * @since 1.2
         */
        public T setPersistenceUnitFactory(Function<InjectionPoint, Object> persistenceUnitFactory) {
            this.persistenceUnitFactory = persistenceUnitFactory;
            return self();
        }

        /**
         * Makes it possible to mock {@code PersistenceContext} injection points.
         *
         * @param persistenceContextFactory
         * @return self
         * @since 1.2
         */
        public T setPersistenceContextFactory(Function<InjectionPoint, Object> persistenceContextFactory) {
            this.persistenceContextFactory = persistenceContextFactory;
            return self();
        }

        protected abstract T self();

        protected abstract I build(Weld weld, List<Object> instancesToInject, Set<Class<? extends Annotation>> scopesToActivate,
                Set<Bean<?>> beans);

        /**
         *
         * @return a new initiator instance
         */
        public I build() {
            return build(weld, instancesToInject.isEmpty() ? Collections.emptyList() : new ArrayList<>(instancesToInject),
                    scopesToActivate.isEmpty() ? Collections.<Class<? extends Annotation>> emptySet()
                            : new HashSet<>(scopesToActivate),
                    beans.isEmpty() ? Collections.<Bean<?>> emptySet() : new HashSet<>(beans));
        }

    }

    protected WeldContainer initWeldContainer(Weld weld) {
        // Register mock injection services if needed
        if (!resources.isEmpty()) {
            weld.addServices(new MockResourceInjectionServices(resources));
        }
        if (ejbFactory != null) {
            weld.addServices(new MockEjbInjectionServices(ejbFactory));
        }
        if (persistenceContextFactory != null || persistenceUnitFactory != null) {
            weld.addServices(new MockJpaInjectionServices(persistenceUnitFactory, persistenceContextFactory));
        }
        // Init the container
        container = weld.initialize();
        if (extension != null) {
            extension.activateContexts();
        }
        injectInstances();
        return container;
    }

    protected void shutdownWeldContainer() {
        try {
            if (extension != null) {
                extension.deactivateContexts();
            }
            releaseInstances();
        } finally {
            if (container != null && container.isRunning()) {
                container.shutdown();
            }
        }
    }
}
