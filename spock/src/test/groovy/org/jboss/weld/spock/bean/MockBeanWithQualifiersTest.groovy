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

import jakarta.enterprise.inject.Any
import org.jboss.weld.junit.MockBean
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import spock.lang.Specification

/**
 * Tests that {@link MockBean} with custom qualifiers has {@link Any} qualifier automatically added.
 *
 * @author Björn Kautler
 */
@EnableWeld
class MockBeanWithQualifiersTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator
            .from(AlternativeMockBeanTest.SimpleService)
            .addBeans(MockBean
                    .builder()
                    .types(String)
                    .qualifiers(Meaty.Literal.INSTANCE)
                    .create { 'foo' }
                    .build())
            .build()

    void 'mock beans with custom qualifiers should have the Any qualifier automatically'() {
        expect:
            weld.select(String, Any.Literal.INSTANCE).get() == 'foo'
    }
}
