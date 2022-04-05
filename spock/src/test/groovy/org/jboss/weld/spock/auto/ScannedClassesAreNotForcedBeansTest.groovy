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

import jakarta.enterprise.inject.Produces
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.auto.beans.unsatisfied.InjectedV8NoAnnotation
import org.jboss.weld.spock.auto.beans.unsatisfied.V8NoAnnotation
import spock.lang.Specification

/**
 * @author Björn Kautler
 */
@EnableWeld(automagic = true)
@AddBeanClasses(InjectedV8NoAnnotation)
class ScannedClassesAreNotForcedBeansTest extends Specification {
    /**
     * V8 is *not* a "bean" class, in that it has no bean defining annotation. To satisfy
     * a dependency on it, a producer method or a reference in an @AddBeanClasses annotation
     * is required.
     * <p>
     * This test ensures that as V8 is discovered via class scanning it is not automatically
     * added as a bean class. If it was added that way, the bean class and producer method would
     * create an ambiguous injection case for V8.
     *
     * NOTE: This case only tests for classes found as non-parameters (e.g. injected fields)
     */

    @Produces
    private V8NoAnnotation engine = new V8NoAnnotation()

    def 'V8NoAnnotation should not be ambiguous for not being incorrectly identified as a bean class'(V8NoAnnotation engine) {
        expect:
            engine != null
    }
}
