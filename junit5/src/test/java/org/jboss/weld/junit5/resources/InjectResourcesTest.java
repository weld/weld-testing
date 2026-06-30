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
package org.jboss.weld.junit5.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.function.Function;

import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Martin Kouba
 */
@EnableWeld
public class InjectResourcesTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.fromTestPackage()
            .bindResource("bar", "hello1")
            .bindResource("java:comp/env/baz", "hello2")
            .setEjbFactory(ip -> new DummySessionBean("ping"))
            .setPersistenceUnitFactory(getPUFactory())
            .setPersistenceContextFactory(getPCFactory()).build();

    @Test
    public void testResourceInjection() {
        FooResources foo = weld.select(FooResources.class).get();
        assertEquals("hello1", foo.bar);
        assertEquals("hello2", foo.baz);
    }

    @Test
    public void testEjbInjection() {
        FooEjbs foo = weld.select(FooEjbs.class).get();
        assertEquals("ping", foo.dummySessionBean.id);
    }

    @Test
    public void testJpaInjection() {
        FooJpa foo = weld.select(FooJpa.class).get();
        assertNotNull(foo.entityManagerFactory);
        assertFalse(foo.entityManagerFactory.isOpen());
        assertNotNull(foo.entityManager);
        assertFalse(foo.entityManager.isOpen());
    }

    // Mock objects

    static Function<InjectionPoint, Object> getPCFactory() {
        return ip -> mock(EntityManager.class);
    }

    static Function<InjectionPoint, Object> getPUFactory() {
        return ip -> mock(EntityManagerFactory.class);
    }

}
