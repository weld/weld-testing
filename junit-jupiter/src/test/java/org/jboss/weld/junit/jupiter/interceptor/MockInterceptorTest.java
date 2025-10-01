/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.junit.jupiter.interceptor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.interceptor.InterceptorBinding;

import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.jboss.weld.testing.MockInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@EnableWeld
public class MockInterceptorTest {

    private List<String> aroundInvokes;

    private List<String> postConstructs;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Foo.class).addBeans(
            MockInterceptor.withBindings(FooBinding.Literal.INSTANCE).aroundInvoke((ctx, b) -> {
                aroundInvokes.add(b.getBeanClass().getName());
                return ctx.proceed();
            }),
            // This interceptor is disabled
            MockInterceptor.withBindings(FooBinding.Literal.INSTANCE).beanClass(String.class).aroundInvoke((ctx, b) -> {
                return false;
            }),
            MockInterceptor.withBindings(FooBinding.Literal.INSTANCE)
                    .postConstruct((ctx, b) -> postConstructs.add(b.getBeanClass().getName())))
            .build();

    @BeforeEach
    public void setup() {
        aroundInvokes = new ArrayList<>();
        postConstructs = new ArrayList<>();
    }

    @Test
    public void testInterception() {
        assertTrue(aroundInvokes.isEmpty());
        assertTrue(postConstructs.isEmpty());
        assertTrue(weld.select(Foo.class).get().ping());
        assertEquals(1, aroundInvokes.size());
        assertEquals(Foo.class.getName(), aroundInvokes.get(0));
        assertEquals(1, postConstructs.size());
        assertEquals(Foo.class.getName(), postConstructs.get(0));
    }

    @Test
    public void testDisabledInterceptor() {
        List<Interceptor<?>> interceptors = weld.getBeanManager().resolveInterceptors(InterceptionType.AROUND_INVOKE,
                FooBinding.Literal.INSTANCE);
        assertEquals(1, interceptors.size());
        assertEquals(MockInterceptor.class, interceptors.get(0).getBeanClass());
    }

    @FooBinding
    static class Foo {

        boolean ping() {
            return true;
        }

    }

    @Target({ TYPE, METHOD })
    @Retention(RUNTIME)
    @Documented
    @InterceptorBinding
    static @interface FooBinding {

        @SuppressWarnings("serial")
        static final class Literal extends AnnotationLiteral<FooBinding> implements FooBinding {

            public static final Literal INSTANCE = new Literal();

        };

    }

}
