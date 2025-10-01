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
package org.jboss.weld.testing;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;

/**
 *
 * @author Martin Kouba
 */
class ContextImpl implements Context {

    private static final Logger LOGGER = Logger.getLogger(ContextImpl.class.getName());

    private final Class<? extends Annotation> scope;

    private final BeanManager beanManager;

    // It's a normal scope so there may be no more than one mapped instance per contextual type per thread
    private final ThreadLocal<Map<Contextual<?>, ContextualInstance<?>>> currentContext = new ThreadLocal<>();

    ContextImpl(Class<? extends Annotation> scope, BeanManager beanManager) {
        this.scope = scope;
        this.beanManager = beanManager;
    }

    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        Map<Contextual<?>, ContextualInstance<?>> ctx = currentContext.get();

        if (ctx == null) {
            // Thread local not set - context is not active!
            throw new ContextNotActiveException();
        }

        ContextualInstance<T> instance = (ContextualInstance<T>) ctx.get(contextual);

        if (instance == null && creationalContext != null) {
            // Bean instance does not exist - create one if we have CreationalContext
            instance = new ContextualInstance<T>(contextual.create(creationalContext), creationalContext, contextual);
            ctx.put(contextual, instance);
        }
        return instance != null ? instance.get() : null;
    }

    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    public boolean isActive() {
        return currentContext.get() != null;
    }

    public void destroy(Contextual<?> contextual) {
        Map<Contextual<?>, ContextualInstance<?>> ctx = currentContext.get();
        if (ctx == null) {
            return;
        }
        ctx.remove(contextual);
    }

    public void activate() {
        currentContext.set(new HashMap<Contextual<?>, ContextualInstance<?>>());
        beanManager.getEvent().select(Initialized.Literal.of(scope)).fire(new Object());
    }

    public void deactivate() {
        Map<Contextual<?>, ContextualInstance<?>> ctx = currentContext.get();
        if (ctx == null) {
            return;
        }
        for (ContextualInstance<?> instance : ctx.values()) {
            try {
                instance.destroy();
            } catch (Exception e) {
                LOGGER.warning("Unable to destroy instance" + instance.get() + " for bean: " + instance.getContextual());
            }
        }
        ctx.clear();
        currentContext.remove();
        beanManager.getEvent().select(Destroyed.Literal.of(scope)).fire(new Object());
    }

    /**
     * This wrapper allows to create and destroy a bean instance properly.
     *
     * @author Martin Kouba
     *
     * @param <T>
     */
    static final class ContextualInstance<T> {

        private final T value;

        private final CreationalContext<T> creationalContext;

        private final Contextual<T> contextual;

        /**
         *
         * @param instance
         * @param creationalContext
         * @param contextual
         */
        ContextualInstance(T instance, CreationalContext<T> creationalContext, Contextual<T> contextual) {
            this.value = instance;
            this.creationalContext = creationalContext;
            this.contextual = contextual;
        }

        T get() {
            return value;
        }

        Contextual<T> getContextual() {
            return contextual;
        }

        void destroy() {
            contextual.destroy(value, creationalContext);
        }

    }

}
