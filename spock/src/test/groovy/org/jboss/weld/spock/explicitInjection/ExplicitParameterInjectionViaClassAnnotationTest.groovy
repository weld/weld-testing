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

package org.jboss.weld.spock.explicitInjection

import jakarta.enterprise.inject.Default
import org.jboss.weld.spock.EnableWeld
import spock.lang.Specification

/**
 * @author Björn Kautler
 */
@EnableWeld(explicitParamInjection = true)
class ExplicitParameterInjectionViaClassAnnotationTest extends Specification {
    @Custom
    void 'parameters with qualifiers should be injected'(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
        expect: 'Bar should be resolved by another extension'
            bar.ping() == CustomExtension.simpleName

        and: 'Foo should be resolved as usual'
            foo.ping() == Foo.simpleName

        and: 'BeanWithQualifier should be resolved'
            bean.ping() == BeanWithQualifier.simpleName
    }
}
