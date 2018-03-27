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
package org.jboss.weld.junit5.enricher.disabled;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.basic.Foo;
import org.jboss.weld.junit5.enricher.FooWeldJunitEnricher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@EnableWeld
public class WeldJunitEnricherDisabledTest {

    @Inject
    Instance<Foo> foo;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(FooWeldJunitEnricher.class.getName(), "false");
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(FooWeldJunitEnricher.class.getName());
    }

    @Test
    void testCustomizer() {
        assertTrue(foo.isUnsatisfied());
    }

}
