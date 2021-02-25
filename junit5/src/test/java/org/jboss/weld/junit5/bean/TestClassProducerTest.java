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
package org.jboss.weld.junit5.bean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Note that we add the class to the deployment so it is recognized as a bean and therefore the producer is also discovered.
 *
 * @author Matej Novotny
 */
@ExtendWith(WeldJunit5Extension.class)
public class TestClassProducerTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(List.class, TestClassProducerTest.class).build();

    @SuppressWarnings("serial")
    @Test
    public void testBean() {
        Assertions.assertEquals("42", weld.select(new TypeLiteral<List<String>>() {
        }).get().get(0));
    }

    @ApplicationScoped
    @Produces
    List<String> produceList() {
        return when(mock(List.class).get(0)).thenReturn("42").getMock();
    }
}
