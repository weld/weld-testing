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
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;

/**
 *
 * @author Martin Kouba
 */
class WeldJunit4Extension implements Extension {

    private final Set<Class<? extends Annotation>> scopesToActivate;

    private final Set<Bean<?>> beans;

    private final List<ContextImpl> contexts;

    WeldJunit4Extension(Set<Class<? extends Annotation>> scopesToActivate, Set<Bean<?>> beans) {
        this.scopesToActivate = scopesToActivate;
        this.beans = beans;
        this.contexts = new ArrayList<>();
    }

    void afterBeandiscovery(@Observes AfterBeanDiscovery event) {
        if (scopesToActivate != null) {
            for (Class<? extends Annotation> scope : scopesToActivate) {
                ContextImpl ctx = new ContextImpl(scope);
                contexts.add(ctx);
                event.addContext(ctx);
            }
        }
        if (beans != null) {
            for (Bean<?> bean : beans) {
                event.addBean(bean);
            }
        }
    }

    void activateContexts() {
        if (contexts.isEmpty()) {
            return;
        }
        for (ContextImpl context : contexts) {
            context.activate();
        }
    }

    void deactivateContexts() {
        if (contexts.isEmpty()) {
            return;
        }
        for (ContextImpl context : contexts) {
            context.deactivate();
        }
    }

}
