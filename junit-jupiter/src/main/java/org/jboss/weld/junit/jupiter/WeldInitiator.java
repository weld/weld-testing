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
package org.jboss.weld.junit.jupiter;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.testing.AbstractWeldInitiator;

/**
 * JUnit 5 initiator - can be used to customize the Weld SE container started by {@link WeldJupiterExtension}.
 *
 * <p>
 * <b>Example:</b>
 *
 * <pre>
 * &#64;ExtendWith(WeldJupiterExtension.class)
 * public class SimpleTest {
 *
 *     &#64;WeldSetup
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
 * @author Matej Novotny
 */
public class WeldInitiator extends AbstractWeldInitiator {

    /**
     * The container is configured with the result of {@link #createWeld()} method and the given bean classes are added. If any
     * of added classes is an extension, it is automatically recognized and enabled.
     *
     * @param beanClasses
     * @return a new WeldInitiator instance
     * @see Weld#beanClasses(Class...)
     */
    public static WeldInitiator of(Class<?>... beanClasses) {
        return from(beanClasses).build();
    }

    /**
     * The container is configured through a provided {@link Weld} instance.
     * <p>
     * Provided instance of {@link Weld} should be kept in the same scope as the resulting {@link WeldInitiator}.
     * I.e. if {@link Weld} instance is kept in a static field, so should be the resulting {@link WeldInitiator}.
     * Failing to uphold this can lead to repetitive configuration of the same {@link Weld} instance which in turn results in
     * unexpected container setup/behavior.
     *
     * @param weld instance of {@link Weld} used to create {@link WeldInitiator}
     * @return a new WeldInitiator instance
     */
    public static WeldInitiator of(Weld weld) {
        return from(weld).build();
    }

    /**
     * The container is configured with the result of {@link #createWeld()} method and all the classes from the test class
     * package are added.
     *
     * @return a new WeldInitiator instance
     */
    public static WeldInitiator ofTestPackage() {
        return fromTestPackage().build();
    }

    /**
     * The container is instructed to do automatic bean discovery, the resulting bean archive is NOT synthetic. Note that this
     * requires beans.xml to be present. It is equals to {@code WeldInitiator.of(new Weld())} invocation.
     *
     * @return a new WeldInitiator instance
     */
    public static WeldInitiator performDefaultDiscovery() {
        return of(new Weld());
    }

    /**
     * Create a builder instance.
     *
     * @param beanClasses
     * @return a builder instance
     * @see #of(Class...)
     */
    @SuppressWarnings("unchecked")
    public static Builder from(Class<?>... beanClasses) {
        Weld weld = createWeld();
        for (Class<?> clazz : beanClasses) {
            if (Extension.class.isAssignableFrom(clazz)) {
                weld.addExtensions((Class<? extends Extension>) clazz);
            } else {
                weld.addBeanClass(clazz);
            }
        }
        return from(weld);
    }

    /**
     * Create a builder instance.
     * <p>
     * Provided instance of {@link Weld} should be kept in the same scope as the resulting {@link WeldInitiator}.
     * I.e. if {@link Weld} instance is kept in a static field, so should be the resulting {@link WeldInitiator}.
     * Failing to uphold this can lead to repetitive configuration of the same {@link Weld} instance which in turn results in
     * unexpected container setup/behavior.
     *
     * @param weld instance of {@link Weld} used as a basis for this {@link WeldInitiator.Builder}
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
     * The returned {@link Weld} instance has:
     * <ul>
     * <li>automatic discovery disabled</li>
     * <li>concurrent deployment disabled</li>
     * </ul>
     *
     * @return a new {@link Weld} instance suitable for testing
     * @see AbstractWeldInitiator#createDefaultWeld()
     */
    public static Weld createWeld() {
        return AbstractWeldInitiator.createDefaultWeld();
    }

    /**
     * This builder can be used to customize the final {@link WeldInitiator} instance, e.g. to activate a context for a given
     * normal scope.
     */
    public static final class Builder extends AbstractBuilder<WeldInitiator, Builder> {

        private Builder(Weld weld) {
            super(weld);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected WeldInitiator build(Weld weld, List<Object> instancesToInject,
                Set<Class<? extends Annotation>> scopesToActivate, Set<Bean<?>> beans) {
            return new WeldInitiator(weld, instancesToInject, scopesToActivate, beans, resources, getEjbFactory(),
                    getPersistenceUnitFactory(), getPersistenceContextFactory());
        }

    }

    private WeldInitiator(Weld weld, List<Object> instancesToInject, Set<Class<? extends Annotation>> scopesToActivate,
            Set<Bean<?>> beans,
            Map<String, Object> resources, Function<InjectionPoint, Object> ejbFactory,
            Function<InjectionPoint, Object> persistenceUnitFactory,
            Function<InjectionPoint, Object> persistenceContextFactory) {
        super(weld, instancesToInject, scopesToActivate, beans, resources, ejbFactory, persistenceUnitFactory,
                persistenceContextFactory);
    }

    void shutdownWeld() {
        super.shutdownWeldContainer();
    }

    WeldContainer initWeld(Object testInstance) {
        Weld weld = WeldInitiator.this.weld;
        if (weld == null) {
            // null in case of fromTestPackage() was used
            weld = createWeld().addPackage(false, testInstance.getClass());
        }

        return initWeldContainer(weld);
    }

    void addObjectsToInjectInto(Set<Object> instancesToInjectInto) {
        for (Object o : instancesToInjectInto) {
            instancesToInject.add(createToInject(o));
        }
    }
}
