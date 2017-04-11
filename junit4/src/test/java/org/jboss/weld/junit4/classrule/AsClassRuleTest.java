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
package org.jboss.weld.junit4.classrule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Martin Kouba
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AsClassRuleTest {

    // The container is shared accross all test methods
    @ClassRule
    public static WeldInitiator weld = WeldInitiator.of(Charlie.class);

    static final AtomicReference<String> CONTAINER_ID = new AtomicReference<String>(null);

    static final AtomicReference<String> CHARLIE_ID = new AtomicReference<String>(null);

    @Test
    public void test1() {
        assertTrue(weld.isRunning());
        CONTAINER_ID.set(weld.getId());
        CHARLIE_ID.set(weld.select(Charlie.class).get().getId());
    }

    @Test
    public void test2() {
        assertTrue(weld.isRunning());
        assertEquals(CONTAINER_ID.get(), weld.getId());
        assertEquals(CHARLIE_ID.get(), weld.select(Charlie.class).get().getId());
    }

}
