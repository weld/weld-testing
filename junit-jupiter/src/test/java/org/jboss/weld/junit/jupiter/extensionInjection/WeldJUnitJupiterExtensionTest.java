/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.junit.jupiter.extensionInjection;

import java.util.Map;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.weld.junit.jupiter.WeldJUnitJupiterExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Basic test for JUnit Jupiter injection into parameter/field handled by Weld
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@ExtendWith(WeldJUnitJupiterExtension.class)
public class WeldJUnitJupiterExtensionTest {

    @Inject
    SomeBean bean;

    @Test
    public void testFieldInjection() {

        // assert field injection works
        Assertions.assertNotNull(bean);
        bean.ping();
    }

    @Test
    public void testparamInjection(FooBean foo) {
        // assert param injection works
        Assertions.assertNotNull(foo);
        foo.ping();
    }

    @Test
    public void testparamInjectionWithQualifier(@MyQualifier BarBean bar) {
        // assert param injection with qualifier works
        Assertions.assertNotNull(bar);
        bar.ping();
    }

    @Nested
    class TestParameterInjectionWithParametrizedTypes {

        @Test
        public void testparamInjectionWithEvent(Event<String> stringEvent) {
            // assert param injection with built-in Event bean works
            Assertions.assertNotNull(stringEvent);
        }

        @Test
        public void testparamInjectionWithInstance(Instance<String> stringInstance) {
            // assert param injection with built-in Instance works
            Assertions.assertNotNull(stringInstance);
        }

        @Test
        public void testparamInjectionWithOtherParametrizedType(Map<String, Integer> map) {
            // assert param injection with ordinary bean with parametrized type works
            Assertions.assertNotNull(map);
            Assertions.assertTrue(map.containsKey("java.util.Map<java.lang.String, java.lang.Integer>"));
        }
    }
}
