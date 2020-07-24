/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc., and individual contributors
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

import org.jboss.weld.junit4.Foo;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.RuleChain;
import org.junit.runners.model.Statement;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Björn Kautler
 */
public class InjectWithClassRuleTest {
    private static WeldInitiator weld = WeldInitiator.from(Foo.class, MeatyStringObserver.class, IamDependent.class)
            .build();

    @ClassRule
    public static RuleChain chain = RuleChain.outerRule((base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            base.evaluate();
            assertTrue(IamDependent.DESTROYED.get());
        }
    }).around(weld);

    @Rule
    public MethodRule testClassInjectorRule = weld.getTestClassInjectorRule();

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

}
