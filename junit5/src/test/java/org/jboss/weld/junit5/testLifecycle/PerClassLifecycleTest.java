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
package org.jboss.weld.junit5.testLifecycle;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Note that we cannot be sure which method executes first - only one of them will do actual verification.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@Isolated
@ExtendWith(WeldJunit5Extension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PerClassLifecycleTest {

    @WeldSetup
    public WeldInitiator initiator = WeldInitiator.of(new Weld(String.valueOf(System.nanoTime()))
            .disableDiscovery().addBeanClass(PlainBean.class));

    String containerId = null;

    @Test
    public void first() {
        if (containerId == null) {
            containerId = WeldContainer.current().getId();
        } else {
            Assertions.assertEquals(containerId, WeldContainer.current().getId());
        }
    }

    @Test
    public void second() {
        if (containerId == null) {
            containerId = WeldContainer.current().getId();
        } else {
            Assertions.assertEquals(containerId, WeldContainer.current().getId());
        }
    }
}
