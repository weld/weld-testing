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

package org.jboss.weld.spock.auto.alternatives

import jakarta.inject.Inject
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.auto.AddBeanClasses
import org.jboss.weld.spock.auto.EnableAlternatives
import spock.lang.Specification

/**
 * Extends {@link BaseEnableAlternativeTest} hence inheriting the annotations. This tests that alternative enablement will be
 * inherited.
 *
 * @author Björn Kautler
 */
class EnableAlternativesInheritanceTest extends BaseEnableAlternativeTest {
    @Inject
    Foo foo

    def 'alternative enablement should be inherited'() {
        expect:
            foo.ping() == FooAlternative.simpleName
    }
}

@EnableWeld(automagic = true)
@AddBeanClasses([Foo, FooAlternative])
@EnableAlternatives(FooAlternative)
class BaseEnableAlternativeTest extends Specification {
}
