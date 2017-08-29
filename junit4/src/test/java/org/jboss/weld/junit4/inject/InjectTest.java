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

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.weld.junit4.Foo;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 * @author Martin Kouba
 */
public class InjectTest {

    @Rule
    public RuleChain chain = RuleChain.outerRule(new TestRule() {
        @Override
        public Statement apply(final Statement base, Description description) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    base.evaluate();
                    assertTrue(IamDependent.DESTROYED.get());
                }
            };
        }
    }).around(WeldInitiator.from(Foo.class, MeatyStringObserver.class, IamDependent.class)
            .inject(this).build());

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
