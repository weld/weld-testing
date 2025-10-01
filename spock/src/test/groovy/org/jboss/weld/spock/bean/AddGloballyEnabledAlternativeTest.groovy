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

import jakarta.enterprise.inject.spi.Bean
import jakarta.enterprise.util.TypeLiteral
import org.jboss.weld.testing.MockBean
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import spock.lang.Isolated
import spock.lang.Specification

/**
 * Tests {@link org.jboss.weld.junit.MockBean} adding a bean that is a globally enabled alternative.
 *
 * @author Björn Kautler
 */
@Isolated
@EnableWeld
class AddGloballyEnabledAlternativeTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator
            .from(Bar)
            .addBeans(createFooAlternativeBean(), createListBean())
            .build()

    static Bean<?> createFooAlternativeBean() {
        return MockBean
                .read(Foo)
                .priority(3)
                .alternative(true)
                .addQualifier(Meaty.Literal.INSTANCE)
                .build()
    }

    Bean<?> createListBean() {
        return MockBean
                .builder()
                .types(new TypeLiteral<List<String>>() {}.type)
                .globallySelectedAlternative(2)
                .creating(Stub(List<String>) {
                    get(0) >> '42'
                })
                .build()
    }

    def 'all alternative beans should be added'() {
        when:
            def bar = weld.select(Bar).get()

        then:
            bar.foo != null
            bar.someList != null
            bar.someList.get(0) == '42'
    }

    def 'all alternative beans can be selected'() {
        when:
            def beans = weld.beanManager.getBeans(Foo, Meaty.Literal.INSTANCE)

        then:
            beans.size() == 1
            beans.first().alternative

        when:
            beans = weld.beanManager.getBeans(new TypeLiteral<List<String>>() {}.type)

        then:
            beans.size() == 1
            beans.first().alternative
    }
}
