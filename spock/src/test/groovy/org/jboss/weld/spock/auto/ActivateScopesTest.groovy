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

import jakarta.enterprise.context.ContextNotActiveException
import jakarta.enterprise.context.ConversationScoped
import jakarta.enterprise.context.RequestScoped
import jakarta.enterprise.context.SessionScoped
import jakarta.enterprise.inject.Produces
import jakarta.enterprise.inject.spi.BeanManager
import jakarta.inject.Named
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.auto.beans.Engine
import org.jboss.weld.spock.auto.beans.V6
import org.jboss.weld.spock.auto.beans.V8
import spock.lang.Specification

/**
 * @author Björn Kautler
 */
@EnableWeld(automagic = true)
@ActivateScopes([SessionScoped, RequestScoped])
class ActivateScopesTest extends Specification {
    @Produces
    @SessionScoped
    @Named('special')
    V8 sessionEngine = new V8()

    @Produces
    @ConversationScoped
    // V6 is annotated with @ApplicationScoped, this tells the container to use this producer instead
    @ExcludeBean
    V6 convoEngine = new V6()

    def '@ActivateScopes should activate the specified scopes'(BeanManager beanManager) {
        expect:
            beanManager.getContext(RequestScoped).active
            beanManager.getContext(SessionScoped).active
    }

    def 'Engine should be resolved to @SessionScoped V8'(@Named("special") Engine engine) {
        expect:
            engine.throttle == 0
    }

    def 'non-activated scopes should fail'(V6 engine) {
        when:
            engine.throttle

        then:
            thrown(ContextNotActiveException)
    }
}
