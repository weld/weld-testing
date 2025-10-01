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

import java.util.function.Function;

import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.injection.spi.helpers.SimpleResourceReference;
import org.jboss.weld.testing.AbstractWeldInitiator.AbstractBuilder;

/**
 *
 * @author Martin Kouba
 * @see AbstractBuilder#setEjbFactory(Function)
 */
public class MockJpaInjectionServices implements JpaInjectionServices {

    private final Function<InjectionPoint, Object> persistenceUnitFactory;

    private final Function<InjectionPoint, Object> persistenceContextFactory;

    public MockJpaInjectionServices(Function<InjectionPoint, Object> persistenceUnitFactory,
            Function<InjectionPoint, Object> persistenceContextFactory) {
        this.persistenceUnitFactory = persistenceUnitFactory;
        this.persistenceContextFactory = persistenceContextFactory;
    }

    @Override
    public ResourceReferenceFactory<EntityManager> registerPersistenceContextInjectionPoint(InjectionPoint injectionPoint) {
        return new ResourceReferenceFactory<EntityManager>() {
            @Override
            public ResourceReference<EntityManager> createResource() {
                if (persistenceContextFactory == null) {
                    throw new IllegalStateException(
                            "Persistent context factory not set, cannot resolve injection point: " + injectionPoint);
                }
                Object context = persistenceContextFactory.apply(injectionPoint);
                if (context == null || context instanceof EntityManager) {
                    return new SimpleResourceReference<EntityManager>((EntityManager) context);
                }
                throw new IllegalStateException("Not an EntityManager instance: " + context);
            }
        };
    }

    @Override
    public ResourceReferenceFactory<EntityManagerFactory> registerPersistenceUnitInjectionPoint(InjectionPoint injectionPoint) {
        return new ResourceReferenceFactory<EntityManagerFactory>() {
            @Override
            public ResourceReference<EntityManagerFactory> createResource() {
                if (persistenceUnitFactory == null) {
                    throw new IllegalStateException(
                            "Persistent unit factory not set, cannot resolve injection point: " + injectionPoint);
                }
                Object unit = persistenceUnitFactory.apply(injectionPoint);
                if (unit == null || unit instanceof EntityManagerFactory) {
                    return new SimpleResourceReference<EntityManagerFactory>((EntityManagerFactory) unit);
                }
                throw new IllegalStateException("Not an EntityManagerFactory instance: " + unit);
            }
        };
    }

    @Override
    public void cleanup() {
    }

}
