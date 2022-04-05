/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.spock.resources

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import spock.lang.Specification

/**
 * @author Björn Kautler
 */
@EnableWeld
class InjectResourcesTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator
            .fromTestPackage()
            .bindResource('bar', 'hello1')
            .bindResource('java:comp/env/baz', 'hello2')
            .setEjbFactory { new DummySessionBean('ping') }
            .setPersistenceUnitFactory { Mock(EntityManagerFactory) }
            .setPersistenceContextFactory { Mock(EntityManager) }
            .build()

    def 'resource injection should work properly'() {
        when:
            def foo = weld.select(FooResources).get()

        then:
            foo.bar == 'hello1'
            foo.baz == 'hello2'
    }

    def 'EJB injection should work properly'() {
        when:
            def foo = weld.select(FooEjbs).get()

        then:
            foo.dummySessionBean.id == 'ping'
    }

    def 'JPA injection should work properly'() {
        when:
            def foo = weld.select(FooJpa).get()

        then:
            foo.entityManagerFactory != null
            !foo.entityManagerFactory.open
            foo.entityManager != null
            !foo.entityManager.open
    }
}
