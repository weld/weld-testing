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
package org.jboss.weld.testing;

import java.util.function.Function;

import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.injection.spi.helpers.SimpleResourceReference;
import org.jboss.weld.testing.AbstractWeldInitiator.AbstractBuilder;

/**
 *
 * @author Martin Kouba
 * @see AbstractBuilder#setEjbFactory(Function)
 */
public class MockEjbInjectionServices implements EjbInjectionServices {

    private final Function<InjectionPoint, Object> ejbFactory;

    public MockEjbInjectionServices(Function<InjectionPoint, Object> ejbFactory) {
        this.ejbFactory = ejbFactory;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public ResourceReferenceFactory<Object> registerEjbInjectionPoint(InjectionPoint injectionPoint) {
        return new ResourceReferenceFactory<Object>() {
            @Override
            public ResourceReference<Object> createResource() {
                return new SimpleResourceReference<Object>(ejbFactory.apply(injectionPoint));
            }
        };
    }

}
