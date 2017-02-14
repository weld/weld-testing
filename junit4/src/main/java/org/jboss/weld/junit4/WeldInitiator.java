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
package org.jboss.weld.junit4;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.ContainerInstance;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule which starts a Weld container per each test method execution:
 *
 * <pre>
 * public class SimpleTest {
 *
 *     &#64;Rule
 *     public WeldInitiator weld = WeldInitiator.of(Foo.class);
 *
 *     &#64;Test
 *     public void testFoo() {
 *         // Weld container is started automatically
 *         // WeldInitiator can be used to perform programmatic lookup of beans
 *         assertEquals("baz", weld.select(Foo.class).get().getBaz());
 *     }
 * }
 * </pre>
 *
 * <p>
 * {@link WeldInitiator} implements {@link Instance} and therefore might be used to perform programmatic lookup of bean instances.
 * </p>
 *
 * @author Martin Kouba
 */
public class WeldInitiator implements TestRule, WeldInstance<Object>, ContainerInstance {

    /**
     * The container is configured with the result of {@link #createWeld()} method and the given bean classes are added.
     *
     * @param beanClasses
     * @return a new test rule
     * @see Weld#beanClasses(Class...)
     */
    public static WeldInitiator of(Class<?>... beanClasses) {
        return of(createWeld().beanClasses(beanClasses));
    }

    /**
     * The container is configured through a provided {@link Weld} instance.
     *
     * @param weld
     * @return a new test rule
     */
    public static WeldInitiator of(Weld weld) {
        return new WeldInitiator(weld, null, null);
    }

    /**
     * The container is configured with the result of {@link #createWeld()} method and all the classes from the test class package are added.
     *
     * @return a new test rule
     */
    public static WeldInitiator ofTestPackage() {
        return new WeldInitiator(null, null, null);
    }

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

    /**
     * Create a builder instance.
     *
     * @param weld
     * @return a builder instance
     * @see #of(Class...)
     */
    public static Builder from(Class<?>... beanClasses) {
        return from(createWeld().beanClasses(beanClasses));
    }

    /**
     * Create a builder instance.
     *
     * @param weld
     * @return a builder instance
     * @see #of(Weld)
     */
    public static Builder from(Weld weld) {
        return new Builder(weld);
    }

    /**
     * Create a builder instance.
     *
     * @return a builder instance
     * @see #ofTestPackage()
     */
    public static Builder fromTestPackage() {
        return new Builder(null);
    }

    /**
     * This builder can be used to customize the final {@link WeldInitiator} instance, e.g. to activate a context for a given normal scope.
     */
    public static final class Builder {

        private final Weld weld;

        private final List<Object> instancesToInject;

        private final Set<Class<? extends Annotation>> scopesToActivate;

        public Builder(Weld weld) {
            this.weld = weld;
            this.instancesToInject = new ArrayList<>();
            this.scopesToActivate = new HashSet<>();
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
        public final Builder activate(Class<? extends Annotation>... normalScopes) {
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
            return this;
        }

        /**
         * Instructs the {@link WeldInitiator} to inject the given non-contextual instance once the container is started, i.e. during test execution.
         * <p>
         * This method could be used e.g. to inject a test class instance:
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
         * @param instance
         * @return self
         */
        public Builder inject(Object instance) {
            this.instancesToInject.add(instance);
            return this;
        }

        /**
         *
         * @return a new {@link WeldInitiator} instance
         */
        public WeldInitiator build() {
            return new WeldInitiator(weld, new ArrayList<>(instancesToInject),
                    new HashSet<>(scopesToActivate));
        }

    }

    private final Weld weld;

    private final List<Object> instancesToInject;

    private final Set<Class<? extends Annotation>> scopesToActivate;

    private final WeldJunit4Extension extension;

    private volatile WeldContainer container;

    private WeldInitiator(Weld weld, List<Object> instancesToInject,
            Set<Class<? extends Annotation>> scopesToActivate) {
        this.instancesToInject = instancesToInject;
        this.scopesToActivate = scopesToActivate;
        this.weld = weld;
        if (hasScopesToActivate()) {
            this.extension = new WeldJunit4Extension(this.scopesToActivate);
            this.weld.addExtension(this.extension);
        } else {
            this.extension = null;
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Weld weld = WeldInitiator.this.weld;
                if (weld == null) {
                    weld = createWeld().addPackage(false, description.getTestClass());
                }
                container = weld.initialize();
                injectInstances();
                if (extension != null) {
                    extension.activateContexts();
                }
                try {
                    base.evaluate();
                } finally {
                    try {
                        if (extension != null) {
                            extension.deactivateContexts();
                        }
                    } finally {
                        if (container != null && container.isRunning()) {
                            container.shutdown();
                        }
                    }
                }
            }
        };
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
     *  @return <code>true</code> if the container was initialized completely and is not shut down yet, <code>false</code> otherwise
     */
    public boolean isRunning() {
        return container.isRunning();
    }

    private void checkContainer() {
        if (container == null || !container.isRunning()) {
            throw new IllegalStateException("Weld container is not running");
        }
    }

    private void injectInstances() {
        if (instancesToInject == null || instancesToInject.isEmpty()) {
            return;
        }
        for (Object instance : instancesToInject) {
            BeanManager beanManager = container.getBeanManager();
            CreationalContext<Object> ctx = beanManager.createCreationalContext(null);
            @SuppressWarnings("unchecked")
            InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) beanManager
                    .createInjectionTarget(beanManager.createAnnotatedType(instance.getClass()));
            injectionTarget.inject(instance, ctx);
        }
    }

    private boolean hasScopesToActivate() {
        return scopesToActivate != null && !scopesToActivate.isEmpty();
    }

}
