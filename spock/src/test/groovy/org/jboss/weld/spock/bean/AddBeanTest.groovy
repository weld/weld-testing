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

package org.jboss.weld.spock.bean

import java.util.concurrent.atomic.AtomicInteger

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.util.TypeLiteral
import org.jboss.weld.testing.MockBean
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import spock.lang.Execution
import spock.lang.Isolated
import spock.lang.Rollup
import spock.lang.Shared
import spock.lang.Specification

import static org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD

/**
 * @author Björn Kautler
 */
@EnableWeld
class AddBeanTest extends Specification {
    @Shared
    def SEQUENCE = new AtomicInteger(0)

    MyService myServiceMock = Mock()

    @WeldSetup
    def weld = WeldInitiator
            .from(Blue)
            .addBeans(MockBean.of(myServiceMock, MyService))
            .addBeans(MockBean.read(BlueToDiscover).scope(Dependent).build())
            .addBeans(createListBean(), createSequenceBean(), createIdSupplierBean())
            .build()

    def createListBean() {
        MockBean
                .builder()
                .types(new TypeLiteral<List<String>>() {}.type)
                .qualifiers(Meaty.Literal.INSTANCE)
                .creating(Stub(List<String>) {
                    get(0) >> '42'
                })
                .build()
    }

    def createSequenceBean() {
        MockBean
                .builder()
                .types(Integer)
                .qualifiers(Meaty.Literal.INSTANCE)
                .create { SEQUENCE.incrementAndGet() }
                .build()
    }

    def createIdSupplierBean() {
        MockBean
                .builder()
                .types(IdSupplier)
                .scope(ApplicationScoped)
                .create { new IdSupplier(UUID.randomUUID().toString()) }
                .build()
    }

    interface MyService {
        void doBusiness(String name)
    }

    static class IdSupplier {
        private final String id

        IdSupplier(String id) {
            this.id = id
        }

        String getId() {
            return id
        }
    }
}

class ConcurrentAddBeanTest extends AddBeanTest {
    def 'Blue should get @Meaty List<String> injected'() {
        expect:
            weld.select(Blue).get().stringList.get(0) == '42'
    }

    @Rollup
    @Execution(SAME_THREAD)
    def 'each Bean.create() should increment the sequence'() {
        expect:
            weld.select(Integer, Meaty.Literal.INSTANCE).get() == i

        where:
            i << (1..10)
    }

    def 'mocking on an injected mock bean should work properly'() {
        given:
            def myService = weld.select(MyService).get()

        when:
            myService.doBusiness('Adalbert')

        then:
            1 * myServiceMock.doBusiness(_)
    }

    def 'application scoped beans should work properly'() {
        when:
            def first = weld.select(IdSupplier).get()
            def second = weld.select(IdSupplier).get()

        then:
            first.id == second.id
    }
}

@Isolated
class IsolatedAddBeanTest extends AddBeanTest {
    def 'the scope of "read" beans should be taken from the manual override'() {
        when:
            def blue1 = weld.select(BlueToDiscover).get()
            def blue2 = weld.select(BlueToDiscover).get()

        then:
            blue1.id != null
            blue2.id != null
            blue1.id != blue2.id
            weld.beanManager.getBeans('blue').size() == 1
    }
}
