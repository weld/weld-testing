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

package org.jboss.weld.spock.extensionInjection

import jakarta.inject.Inject
import org.jboss.weld.spock.EnableWeld
import spock.lang.Execution
import spock.lang.Rollup
import spock.lang.Shared
import spock.lang.Specification

import static org.jboss.weld.spock.EnableWeld.Scope.FEATURE
import static org.jboss.weld.spock.EnableWeld.Scope.SPECIFICATION
import static org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD

/**
 * Basic test for Spock injection into parameter/field handled by Weld
 *
 * @author Björn Kautler
 */
@EnableWeld
@Execution(SAME_THREAD)
class SpockInjectionTest extends Specification {
    @Inject
    SomeBean bean

    @Inject
    @Shared
    SomeBean sharedBean

    def setupBean

    def setup(SomeBean setupBean) {
        this.setupBean = setupBean
    }

    def 'field should be injected'() {
        expect:
            bean != null
            bean.ping()
    }

    def 'shared field should be injected with scope ITERATION'() {
        expect:
            sharedBean != null
            sharedBean.ping()
    }

    @EnableWeld(scope = FEATURE)
    def 'shared field should be injected with scope FEATURE'() {
        expect:
            sharedBean != null
            sharedBean.ping()
    }

    @Rollup
    @EnableWeld(scope = FEATURE)
    def 'shared field should be injected with scope FEATURE for data-driven feature'() {
        expect:
            sharedBean != null
            sharedBean.ping()

        where:
            i = 1
    }

    def 'parameter should be injected'(FooBean foo) {
        expect:
            foo != null
            foo.ping()
    }

    def 'parameter with qualifier should be injected'(@MyQualifier BarBean bar) {
        expect:
            bar != null
            bar.ping()
    }

    def 'parameter should be injected into setup method'() {
        expect:
            setupBean != null
            setupBean.ping()
    }
}

@EnableWeld(scope = SPECIFICATION)
class SpecificationSpockInjectionTest extends Specification {
    @Inject
    @Shared
    SomeBean sharedBean

    @Shared
    def setupSpecBean

    def setupSpec(SomeBean setupSpecBean) {
        this.setupSpecBean = setupSpecBean
    }

    def 'shared field should be injected with scope SPECIFICATION'() {
        expect:
            sharedBean != null
            sharedBean.ping()
    }

    def 'parameter should be injected into setupSpec method with scope SPECIFICATION'() {
        expect:
            setupSpecBean != null
            setupSpecBean.ping()
    }
}
