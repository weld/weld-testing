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

package org.jboss.weld.spock.interceptor

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.Target

import jakarta.enterprise.util.AnnotationLiteral
import jakarta.interceptor.InterceptorBinding
import org.jboss.weld.junit.MockInterceptor
import org.jboss.weld.spock.EnableWeld
import org.jboss.weld.spock.WeldInitiator
import org.jboss.weld.spock.WeldSetup
import spock.lang.Specification

import static jakarta.enterprise.inject.spi.InterceptionType.AROUND_INVOKE
import static java.lang.annotation.ElementType.METHOD
import static java.lang.annotation.ElementType.TYPE
import static java.lang.annotation.RetentionPolicy.RUNTIME

/**
 * @author Björn Kautler
 */
@EnableWeld
class MockInterceptorTest extends Specification {
    def aroundInvokes = []

    def postConstructs = []

    @WeldSetup
    def weld = WeldInitiator
            .from(Foo)
            .addBeans(
                    MockInterceptor
                            .withBindings(BarBinding.Literal.INSTANCE)
                            .aroundInvoke { ctx, b ->
                                aroundInvokes.add(b.beanClass.name)
                                ctx.proceed()
                            },
                    // This interceptor is disabled
                    MockInterceptor
                            .withBindings(BarBinding.Literal.INSTANCE)
                            .beanClass(String)
                            .aroundInvoke { ctx, b ->
                                false
                            },
                    MockInterceptor
                            .withBindings(FooBinding.Literal.INSTANCE)
                            .postConstruct { ctx, b ->
                                postConstructs.add(b.beanClass.name)
                            })
            .build()

    def 'interception should work properly'() {
        expect:
            aroundInvokes.empty
            postConstructs.empty

        when:
            def pong = weld.select(Foo).get().ping()

        then:
            pong
            verifyAll {
                aroundInvokes == [Foo.name]
                postConstructs == [Foo.name]
            }
    }

    def 'disabled interceptor should not be resolved'() {
        when:
            def interceptors = weld.beanManager.resolveInterceptors(AROUND_INVOKE, BarBinding.Literal.INSTANCE)

        then:
            interceptors*.beanClass == [MockInterceptor]
    }

    @FooBinding
    static class Foo {
        @BarBinding
        boolean ping() {
            return true
        }
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    @Documented
    @InterceptorBinding
    static @interface FooBinding {
        static final class Literal extends AnnotationLiteral<FooBinding> implements FooBinding {
            public static final Literal INSTANCE = new Literal()
        }
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    @Documented
    @InterceptorBinding
    static @interface BarBinding {
        static final class Literal extends AnnotationLiteral<BarBinding> implements BarBinding {
            public static final Literal INSTANCE = new Literal()
        }
    }
}
