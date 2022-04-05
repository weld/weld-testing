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

package org.jboss.weld.spock.event

import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import org.jboss.weld.spock.basic.Foo
import spock.lang.Specification

/**
 * @author Björn Kautler
 */
@EnableWeld
class FireEventTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator.of(DummyObserver)

    def 'event should be fired'() {
        given:
            DummyObserver.MESSAGES.clear()

        when: 'Fire an event'
            weld.event().select(Foo).fire(new Foo())

        then:
            DummyObserver.MESSAGES.size() == 1
            DummyObserver.MESSAGES.first().bar == null
    }
}
