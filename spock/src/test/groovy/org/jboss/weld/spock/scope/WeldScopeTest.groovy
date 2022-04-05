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

package org.jboss.weld.spock.scope

import org.jboss.weld.environment.se.Weld
import org.jboss.weld.environment.se.WeldContainer
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import spock.lang.Isolated
import spock.lang.Rollup
import spock.lang.Shared
import spock.lang.Specification

import static org.jboss.weld.spock.EnableWeld.Scope.FEATURE
import static org.jboss.weld.spock.EnableWeld.Scope.SPECIFICATION

/**
 * @author Björn Kautler
 */
class WeldScopeTest extends Specification {
    @WeldSetup
    def initiator = WeldInitiator.of(new Weld(String.valueOf(System.nanoTime()))
            .disableDiscovery().addBeanClass(PlainBean))

    @Shared
    def containerId
}

@Isolated
@EnableWeld(scope = SPECIFICATION)
class SpecificationWeldScopeTest extends WeldScopeTest {
    @Rollup
    def 'all iterations should use the same container with scope SPECIFICATION'() {
        given:
            if (containerId == null) {
                containerId = WeldContainer.current().id
            }

        expect:
            containerId == WeldContainer.current().id

        where:
            i << (1..2)
    }
}

@Isolated
class OthersWeldScopeTest extends WeldScopeTest {
    @Rollup
    @EnableWeld(scope = FEATURE)
    def 'all iterations should use the same container with scope FEATURE'() {
        given:
            if (containerId == null) {
                containerId = WeldContainer.current().id
            }

        expect:
            containerId == WeldContainer.current().id

        where:
            i << (1..2)
    }

    @Rollup
    @EnableWeld
    def 'all iterations should use different containers with scope ITERATION'() {
        given:
            if (containerId == null) {
                containerId = WeldContainer.current().id
            }

        expect:
            containerId != WeldContainer.current().id

        where:
            i << (1..2)
    }
}
