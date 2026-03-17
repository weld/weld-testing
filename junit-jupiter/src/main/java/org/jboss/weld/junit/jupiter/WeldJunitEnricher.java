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
package org.jboss.weld.junit.jupiter;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit.jupiter.WeldInitiator.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * If no {@link WeldInitiator} field annotated with {@link WeldSetup} is present on a test class, all service providers of this
 * interface are used to enrich the
 * default test environment. The initial {@link Weld} instance is created using {@link WeldInitiator#createWeld()}.
 *
 * <p>
 * A system property with key equal to FQCN of a customizer class may be used to disable an enricher completely. E.g. for a
 * class
 * {@code org.weld.FooEnricher} use {@code -Dorg.weld.FooEnricher=false} to disable the enricher.
 * </p>
 *
 * @author Martin Kouba
 */
public interface WeldJunitEnricher {

    /**
     * Enrich the default test environment.
     *
     * <p>
     * {@link Weld#initialize()} and {@link WeldInitiator.Builder#build()} methods must never be invoked in an enricher!
     * </p>
     *
     * @param testInstance
     * @param context
     * @param weld
     * @param weldInitiatorBuilder
     */
    void enrich(Object testInstance, ExtensionContext context, Weld weld, Builder weldInitiatorBuilder);

}
