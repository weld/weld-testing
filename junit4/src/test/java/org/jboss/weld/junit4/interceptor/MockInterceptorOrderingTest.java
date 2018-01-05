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
package org.jboss.weld.junit4.interceptor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.InterceptorBinding;

import org.jboss.weld.junit.MockInterceptor;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MockInterceptorOrderingTest {

    private static AtomicBoolean guard = new AtomicBoolean();

    @Rule
    public WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld().addBeanClass(Foo.class).enableInterceptors(Integer.class, String.class)).addBeans(
            MockInterceptor.withBindings(FooBinding.Literal.INSTANCE).beanClass(Integer.class).aroundInvoke((ctx, b) -> {
                return 5;
                }),
            MockInterceptor.withBindings(FooBinding.Literal.INSTANCE).beanClass(String.class).aroundInvoke((ctx, b) -> {
                // Should be never invoked
                guard.set(true);
                return -5;
                })).build();

    @Before
    public void setup() {
        guard.set(false);
    }

    @Test
    public void testOrdering() {
        assertFalse(guard.get());
        assertEquals(5, weld.select(Foo.class).get().ping());
    }

    @FooBinding
    static class Foo {

        int ping() {
            // Should be never invoked
            guard.set(true);
            return 1;
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
