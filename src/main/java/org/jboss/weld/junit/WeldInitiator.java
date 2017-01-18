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
import java.util.Iterator;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule which allows to start a Weld container per test method execution.
 *
 * <p>
 * {@link WeldInitiator} implements {@link Instance} and therefore might be used to perform programmatic lookup of bean instances.
 * </p>
 *
 * @author Martin Kouba
 */
public class WeldInitiator implements TestRule, WeldInstance<Object> {

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
        return new WeldInitiator(weld);
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
        return new Weld().disableDiscovery().property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false);
    }

    private final Weld weld;

    private WeldContainer container;

    private WeldInitiator(Weld weld) {
        this.weld = weld;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                container = weld.initialize();
                try {
                    base.evaluate();
                } finally {
                    if (container != null && container.isRunning()) {
                        container.shutdown();
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

    private void checkContainer() {
        if (container == null || !container.isRunning()) {
            throw new IllegalStateException("Weld container is not running");
        }
    }

}
