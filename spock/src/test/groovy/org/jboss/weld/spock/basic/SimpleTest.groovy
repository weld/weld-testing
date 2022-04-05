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

package org.jboss.weld.spock.basic

import jakarta.inject.Inject
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import org.jboss.weld.spock.ofpackage.Alpha
import spock.lang.Issue
import spock.lang.Specification

/**
 * @author Björn Kautler
 */
@EnableWeld
class SimpleTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator.of(Foo)

    def 'test Foo A'() {
        expect:
            weld.select(Foo).get().bar == 'baz'
            !weld.select(Alpha).resolvable
    }

    @Issue('https://github.com/weld/weld-junit/issues/19')
    void 'test Foo B'() {
        expect:
            weld.select(Foo).get().bar == 'baz'
            !weld.select(Alpha).resolvable
    }

    def 'manual non-contextual injection should work properly'() {
        given:
            def sut = new NonContextual()

        when:
            def contextReleaser = weld.injectNonContextual(sut)

        then:
            sut.foo.bar == 'baz'

        cleanup:
            contextReleaser.close()
    }

    private static class NonContextual {
        @Inject
        private Foo foo
    }
}
