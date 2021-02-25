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
package org.jboss.weld.junit4.inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.jboss.weld.junit4.Foo;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runners.model.Statement;

/**
 *
 * @author Martin Kouba
 */
public class InjectTest {

    private final WeldInitiator weld = WeldInitiator
            .from(Foo.class, MeatyStringObserver.class, IamDependent.class)
            .inject(this)
            .build();

    @Rule
    public RuleChain chain = RuleChain.outerRule((base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            base.evaluate();
            assertTrue(IamDependent.DESTROYED.get());
        }
    }).around(weld);

    @Inject
    Foo foo;

    @Inject
    @Meaty
    Event<String> event;

    @Inject
    IamDependent iamDependent;

    @Test
    public void testFoo() {
        MeatyStringObserver.MESSAGES.clear();
        assertEquals("baz", foo.getBar());
        event.fire("hello");
        assertEquals(1, MeatyStringObserver.MESSAGES.size());
        assertEquals("hello", MeatyStringObserver.MESSAGES.get(0));
        iamDependent.bang();
    }

    @Test
    public void testManualNonContextualInjection() throws Exception {
        final InjectTest sut = new InjectTest();
        try (AutoCloseable contextReleaser = weld.injectNonContextual(sut)) {
            MeatyStringObserver.MESSAGES.clear();
            assertEquals("baz", sut.foo.getBar());
            sut.event.fire("hello");
            assertEquals(1, MeatyStringObserver.MESSAGES.size());
            assertEquals("hello", MeatyStringObserver.MESSAGES.get(0));
            sut.iamDependent.bang();
        }
        assertTrue(IamDependent.DESTROYED.get());
    }

}
