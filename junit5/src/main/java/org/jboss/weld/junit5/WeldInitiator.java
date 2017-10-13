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
package org.jboss.weld.junit5;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.junit.AbstractWeldInitiator;

/**
 * TODO document this
 *
 * @author Matej Novotny
 */
public class WeldInitiator extends AbstractWeldInitiator {

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
        return from(weld).build();
    }

    /**
     * The container is configured with the result of {@link #createWeld()} method and all the classes from the test class
     * package are added.
     *
     * @return a new test rule
     */
    public static WeldInitiator ofTestPackage() {
        return fromTestPackage().build();
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
     * This builder can be used to customize the final {@link WeldInitiator} instance, e.g. to activate a context for a given
     * normal scope.
     */
    public static final class Builder extends AbstractBuilder {

        public Builder(Weld weld) {
            super(weld);
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
            return (Builder) super.activate(normalScopes);
        }

        public Builder inject(Object instance) {
            return (Builder) super.inject(instance);
        }

        public Builder addBeans(Bean<?>... beans) {
            return (Builder) super.addBeans(beans);
        }

        /**
         *
         * @return a new {@link WeldInitiator} instance
         */
        @Override
        public WeldInitiator build() {
            return new WeldInitiator(weld,
                instancesToInject.isEmpty() ? Collections.emptyList()
                    : new ArrayList<>(instancesToInject),
                scopesToActivate.isEmpty()
                    ? Collections.<Class<? extends Annotation>>emptySet()
                    : new HashSet<>(scopesToActivate),
                beans.isEmpty() ? Collections.<Bean<?>>emptySet() : new HashSet<>(beans));
        }

    }

    // has to be package protected so we can init this from WeldJunit5Extension is user doesn's specify it
    WeldInitiator(Weld weld, List<Object> instancesToInject,
        Set<Class<? extends Annotation>> scopesToActivate, Set<Bean<?>> beans) {
        super(weld, instancesToInject, scopesToActivate, beans);
    }

    void shutdownWeld() {
        super.shutdownWeldContainer();
    }

    WeldContainer initWeld(Object testInstance) {
        Weld weld = WeldInitiator.this.weld;
        if (weld == null) {
            weld = createWeld().addPackage(false, testInstance.getClass());
        }
        // this ensures the test instance is always an InjectionTarget
        instancesToInject.add(createToInject(testInstance));

        return initWeldContainer(weld);
    }
}
