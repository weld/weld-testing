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
package org.jboss.weld.junit.jupiter.bean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldJUnitJupiterExtension;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.jboss.weld.testing.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author Matej Novotny
 */
@ExtendWith(WeldJUnitJupiterExtension.class)
public class AddPassivatingBeanTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(List.class).addBeans(createListBean()).activate(SessionScoped.class).build();

    @SuppressWarnings("serial")
    static Bean<?> createListBean() {
        return MockBean.builder()
                .types(new TypeLiteral<List<String>>() {
                }.getType())
                .scope(SessionScoped.class)
                .creating(
                        // Mock object provided by Mockito
                        when(mock(List.class).get(0)).thenReturn("42").getMock())
                .build();
    }

    @SuppressWarnings("serial")
    @Test
    public void testPassivatingBeanAdded() {
        Assertions.assertEquals("42", weld.select(new TypeLiteral<List<String>>() {
        }).get().get(0));
    }

}
