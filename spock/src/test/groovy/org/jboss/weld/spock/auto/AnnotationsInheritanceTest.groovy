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

import jakarta.inject.Inject
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.auto.beans.Engine
import org.jboss.weld.spock.auto.beans.V8
import org.jboss.weld.spock.auto.extension.AddedExtension
import org.jboss.weld.spock.auto.interceptorAndDecorator.DecoratedBean
import org.jboss.weld.spock.auto.interceptorAndDecorator.InterceptedBean
import org.jboss.weld.spock.auto.interceptorAndDecorator.TestDecorator
import org.jboss.weld.spock.auto.interceptorAndDecorator.TestInterceptor
import spock.lang.Specification

/**
 * Tests the inheritance of "Weld-Spock" annotations from test parent classes.
 *
 * @author Björn Kautler
 */
@EnableWeld(automagic = true)
@AddBeanClasses(V8)
class BaseAddBeanClassesTest extends Specification {
}

class AnnotationsInheritanceAddBeanClassesTest extends BaseAddBeanClassesTest {
    @Inject
    private Engine engine

    def '@AddBeanClasses should pull in V8 to fulfill the injected Engine interface'() {
        expect:
            engine != null
    }
}

@EnableWeld(automagic = true)
@AddEnabledDecorators(TestDecorator)
class BaseAddDecoratorTest extends Specification {
}

class AnnotationsInheritanceAddDecoratorTest extends BaseAddDecoratorTest {
    @Inject
    DecoratedBean bean

    def 'bean should be decorated'() {
        expect:
            bean.ping() == TestDecorator.toString() + DecoratedBean.toString()
    }
}

@EnableWeld(automagic = true)
@AddExtensions(AddedExtension)
class BaseAddExtensionsTest extends Specification {
}

class AnnotationsInheritanceAddExtensionsTest extends BaseAddExtensionsTest {
    def '@AddExtensions should add the specified extensions'() {
        expect:
            AddedExtension.enabled
    }
}

@EnableWeld(automagic = true)
@AddEnabledInterceptors(TestInterceptor)
class BaseAddInterceptorTest extends Specification {
}

class AnnotationsInheritanceAddInterceptorTest extends BaseAddInterceptorTest {
    @Inject
    InterceptedBean bean

    def 'bean should be intercepted'() {
        expect:
            bean.ping() == TestInterceptor.toString() + InterceptedBean.toString()
    }
}

@EnableWeld(automagic = true)
@AddPackages(Engine)
class BaseAddPackagesTest {
}

class AnnotationsInheritanceAddPackagesTest extends BaseAddPackagesTest {
    @Inject
    private V8 engine

    def '@AddPackages should pull in V8 (without bean defining annotation) to fulfill the injected Engine interface'() {
        expect:
            engine != null
    }
}
