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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.ContainerInstance;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class AbstractWeldInitiator implements WeldInstance<Object>, ContainerInstance {

    /**
     * The returned {@link Weld} instance has:
     * <ul>
     * <li>automatic discovery disabled</li>
     * <li>concurrent deployment disabled</li>
     * </ul>
     *
     * @return a new {@link Weld} instance suitable for testing
     */
    public static Weld createWeld() {
        return new Weld().disableDiscovery().property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(),
            false);
    }

    protected final Weld weld;

    protected final List<ToInject> instancesToInject;

    protected final Set<Class<? extends Annotation>> scopesToActivate;

    protected final Set<Bean<?>> beans;

    protected final WeldCDIExtension extension;

    protected volatile WeldContainer container;

    protected AbstractWeldInitiator(Weld weld, List<Object> instancesToInject,
        Set<Class<? extends Annotation>> scopesToActivate, Set<Bean<?>> beans) {
        this.instancesToInject = new ArrayList<>();
        for (Object instance : instancesToInject) {
            this.instancesToInject.add(createToInject(instance));
        }
        this.scopesToActivate = scopesToActivate;
        this.beans = beans;
        this.weld = weld;
        if (hasScopesToActivate() || hasBeansToAdd()) {
            this.extension = new WeldCDIExtension(this.scopesToActivate, this.beans);
            this.weld.addExtension(this.extension);
        } else {
            this.extension = null;
        }
    }

    protected ToInject createToInject(Object instanceToInject) {
        return new ToInject(instanceToInject);
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
    public Handler<Object> getHandler() {
        checkContainer();
        return container.getHandler();
    }

    @Override
    public boolean isResolvable() {
        checkContainer();
        return container.isResolvable();
    }

    @Override
    public Iterable<Handler<Object>> handlers() {
        checkContainer();
        return container.handlers();
    }

    @Override
    public void destroy(Object instance) {
        checkContainer();
        container.destroy(instance);
    }

    /**
     * Allows to fire events.
     *
     * @return an event object
     */
    public Event<Object> event() {
        checkContainer();
        return container.event();
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
        return container.isRunning();
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

    protected static abstract class AbstractBuilder<I extends AbstractWeldInitiator, T extends AbstractBuilder<I, T>> {

        protected final Weld weld;

        protected final List<Object> instancesToInject;

        protected final Set<Class<? extends Annotation>> scopesToActivate;

        protected final Set<Bean<?>> beans;

        public AbstractBuilder(Weld weld) {
            this.weld = weld;
            this.instancesToInject = new ArrayList<>();
            this.scopesToActivate = new HashSet<>();
            this.beans = new HashSet<>();
        }

        /**
         * Activate and deactivate contexts for the given normal scopes for each test method execution.
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
                    throw new IllegalArgumentException(
                        "Only annotations annotated with @NormalScope are supported!");
                }
                this.scopesToActivate.add(scope);
            }
            return self();
        }

        /**
         * Instructs the initiator to inject the given non-contextual instance once the container is started, i.e.
         * during test execution.
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
         * Injected {@link Dependent} bean instances are destroyed after the test execution. However, the licecycle of the
         * non-contextual instance is not managed by the container and all injected references will be invalid after the test
         * execution.
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
         * @since 1.1
         */
        public T addBeans(Bean<?>... beans) {
            Collections.addAll(this.beans, beans);
            return self();
        }

        protected abstract T self();

        protected abstract I build(Weld weld, List<Object> instancesToInject,
                Set<Class<? extends Annotation>> scopesToActivate, Set<Bean<?>> beans);

        /**
         *
         * @return a new initiator instance
         */
        public I build() {
            return build(weld, instancesToInject.isEmpty() ? Collections.emptyList() : new ArrayList<>(instancesToInject),
                    scopesToActivate.isEmpty() ? Collections.<Class<? extends Annotation>> emptySet() : new HashSet<>(scopesToActivate),
                    beans.isEmpty() ? Collections.<Bean<?>> emptySet() : new HashSet<>(beans));
        }

    }

    protected WeldContainer initWeldContainer(Weld weld) {
        container = weld.initialize();
        injectInstances();
        if (extension != null) {
            extension.activateContexts();
        }
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
