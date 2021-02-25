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
package org.jboss.weld.junit5.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.junit5.ofpackage.Alpha;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

/**
 *
 * @author Matej Novotny
 */
@ExtendWith(WeldJunit5Extension.class)
public class SimpleTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(Foo.class);

    @Test
    public void testFooA() {
        assertEquals("baz", weld.select(Foo.class).get().getBar());
        assertFalse(weld.select(Alpha.class).isResolvable());
    }

    // https://github.com/weld/weld-junit/issues/19
    @Test
    public void testFooB() {
        assertEquals("baz", weld.select(Foo.class).get().getBar());
        assertFalse(weld.select(Alpha.class).isResolvable());
    }

    @Test
    public void testManualNonContextualInjection() throws Exception {
        final NonContextual sut = new NonContextual();
        try (AutoCloseable contextReleaser = weld.injectNonContextual(sut)) {
            assertEquals("baz", sut.foo.getBar());
        }
    }

    private static class NonContextual {
        @Inject
        private Foo foo;
    }
}
