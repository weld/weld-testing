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
package org.jboss.weld.junit.jupiter.contexts.events;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;

import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Martin Kouba
 */
@EnableWeld
public class ContextLifecycleEventsTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(ContextLifecycleEventsObserver.class)
            .activate(RequestScoped.class, SessionScoped.class).build();

    @Test
    public void testInitFired() {
        // At this time @Initialized should be fired
        assertTrue(ContextLifecycleEventsObserver.EVENTS.contains(Initialized.class.getName() + RequestScoped.class.getName()));
        assertTrue(ContextLifecycleEventsObserver.EVENTS.contains(Initialized.class.getName() + SessionScoped.class.getName()));
    }

    @AfterAll
    public static void afterAll() {
        // At this time @Destroyed from the previous test method should be fired
        assertTrue(ContextLifecycleEventsObserver.EVENTS.contains(Destroyed.class.getName() + RequestScoped.class.getName()));
        assertTrue(ContextLifecycleEventsObserver.EVENTS.contains(Destroyed.class.getName() + SessionScoped.class.getName()));
    }

}
