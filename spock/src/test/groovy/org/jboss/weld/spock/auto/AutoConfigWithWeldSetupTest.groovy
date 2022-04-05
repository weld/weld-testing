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

package org.jboss.weld.spock.auto

import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import org.jboss.weld.spock.util.EmbeddedSpecRunnerWrapper
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Specification

/**
 * @author Björn Kautler
 */
class AutoConfigWithWeldSetupTest extends Specification {
    def '@WeldSetup should not be compatible with automagic mode'() {
        given:
            def runner = new EmbeddedSpecRunnerWrapper()
            runner.addClassImport(EnableWeld)
            runner.addClassImport(WeldSetup)
            runner.addClassImport(WeldInitiator)
            runner.addClassMemberImport(WeldInitiator)

        when:
            runner.runWithImports '''
                @EnableWeld(automagic = true)
                class Foo extends Specification {
                    @WeldSetup
                    def weld = WeldInitiator.of(createWeld())

                    def bar() {
                        expect:
                            'IllegalStateException expected' == 'before feature method is entered'
                    }
                }
            '''

        then:
            InvalidSpecException ise = thrown()
            ise.message == '''
                When using automagic mode, no @WeldSetup annotated field should be present! Fields found:
                Field 'weld' with type class java.lang.Object which is in Foo
            '''.stripIndent(true).trim()
    }
}
