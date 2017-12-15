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

import java.util.function.Function;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.injection.spi.helpers.SimpleResourceReference;
import org.jboss.weld.junit.AbstractWeldInitiator.AbstractBuilder;

/**
 *
 * @author Martin Kouba
 * @see AbstractBuilder#setEjbFactory(Function)
 */
public class MockJpaInjectionServices implements JpaInjectionServices {

    private final Function<InjectionPoint, Object> persistenceUnitFactory;

    private final Function<InjectionPoint, Object> persistenceContextFactory;

    public MockJpaInjectionServices(Function<InjectionPoint, Object> persistenceUnitFactory, Function<InjectionPoint, Object> persistenceContextFactory) {
        this.persistenceUnitFactory = persistenceUnitFactory;
        this.persistenceContextFactory = persistenceContextFactory;
    }

    @Override
    public ResourceReferenceFactory<EntityManager> registerPersistenceContextInjectionPoint(InjectionPoint injectionPoint) {
        return new ResourceReferenceFactory<EntityManager>() {
            @Override
            public ResourceReference<EntityManager> createResource() {
                return new SimpleResourceReference<EntityManager>(resolvePersistenceContext(injectionPoint));
            }
        };
    }

    @Override
    public ResourceReferenceFactory<EntityManagerFactory> registerPersistenceUnitInjectionPoint(InjectionPoint injectionPoint) {
        return new ResourceReferenceFactory<EntityManagerFactory>() {
            @Override
            public ResourceReference<EntityManagerFactory> createResource() {
                return new SimpleResourceReference<EntityManagerFactory>(resolvePersistenceUnit(injectionPoint));
            }
        };
    }

    @Override
    public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint) {
        if (persistenceContextFactory == null) {
            throw new IllegalStateException("Persistent context factory not set, cannot resolve injection point: " + injectionPoint);
        }
        Object context = persistenceContextFactory.apply(injectionPoint);
        if (context == null || context instanceof EntityManager) {
            return (EntityManager) context;
        }
        throw new IllegalStateException("Not an EntityManager instance: " + context);
    }

    @Override
    public EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint) {
        if (persistenceUnitFactory == null) {
            throw new IllegalStateException("Persistent unit factory not set, cannot resolve injection point: " + injectionPoint);
        }
        Object unit = persistenceUnitFactory.apply(injectionPoint);
        if (unit == null || unit instanceof EntityManagerFactory) {
            return (EntityManagerFactory) unit;
        }
        throw new IllegalStateException("Not an EntityManagerFactory instance: " + unit);
    }

    @Override
    public void cleanup() {
    }

}
