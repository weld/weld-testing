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

package org.jboss.weld.spock.enricher

import jakarta.inject.Inject
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.basic.Foo
import spock.lang.ResourceLock
import spock.lang.Specification

import static org.spockframework.runtime.model.parallel.ResourceAccessMode.READ
import static org.spockframework.runtime.model.parallel.Resources.SYSTEM_PROPERTIES

/**
 * @author Björn Kautler
 */
@EnableWeld
@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ)
class WeldSpockEnricherTest extends Specification {
    @Inject
    Foo foo

    def 'weld spock enricher should do its work'() {
        expect:
            foo.bar == 'baz'
    }
}
