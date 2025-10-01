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
package org.jboss.weld.junit.jupiter.enricher;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit.jupiter.WeldInitiator.Builder;
import org.jboss.weld.junit.jupiter.WeldJunitEnricher;
import org.jboss.weld.junit.jupiter.basic.Foo;
import org.jboss.weld.junit.jupiter.enricher.disabled.WeldJunitEnricherDisabledTest;
import org.junit.jupiter.api.extension.ExtensionContext;

public class FooWeldJunitEnricher implements WeldJunitEnricher {

    @Override
    public void enrich(Object testInstance, ExtensionContext context, Weld weld,
            Builder weldInitiatorBuilder) {
        if (WeldJunitEnricherTest.class.equals(testInstance.getClass())
                || WeldJunitEnricherDisabledTest.class.equals(testInstance.getClass())) {
            weld.addBeanClass(Foo.class);
        }
    }

}
