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

package org.jboss.weld.spock.alternative

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.weld.testing.MockBean
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import spock.lang.Issue
import spock.lang.Specification

import static org.jboss.weld.spock.WeldInitiator.createWeld

/**
 * This mimics problems in issue 64, but without the need for discovery
 *
 * @author Björn Kautler
 */
@EnableWeld
class AlternativeAsSoleBeanInSyntheticArchiveTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator
            .from(createWeld())
            .addBeans(createSelectedAlternativeBean())
            .build()

    static createSelectedAlternativeBean() {
        return MockBean
                .builder()
                .types(Fish)
                .scope(ApplicationScoped)
                .selectedAlternative(Fish)
                .creating(new Fish(200))
                .build()
    }

    @Inject
    Fish fish

    @Issue('https://github.com/weld/weld-junit/issues/64')
    def 'synthetic archive with only enabled alternative should still work'() {
        expect:
            fish.numberOfLegs == 200
    }
}
