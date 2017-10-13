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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit.MockBean.CreateFunction;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 *
 * @author Matej Novotny
 */
@ExtendWith(WeldJunit5Extension.class)
public class AddBeanTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Blue.class)
        .addBeans(MockBean.of(Mockito.mock(MyService.class), MyService.class), createListBean(),
            createSequenceBean(), createIdSupplierBean())
        .build();

    @SuppressWarnings("serial")
    static Bean<?> createListBean() {
        return MockBean.builder()
            .types(new TypeLiteral<List<String>>() {
            }.getType())
            .qualifiers(Meaty.Literal.INSTANCE)
            .creating(
                // Mock object provided by Mockito
                Mockito.when(Mockito.mock(List.class).get(0)).thenReturn("42").getMock())
            .build();
    }

    static Bean<?> createSequenceBean() {
        return MockBean.<Integer>builder()
            .types(Integer.class)
            .qualifiers(Meaty.Literal.INSTANCE)
            .create((ctx) -> SEQUENCE.incrementAndGet()).build();
//                        // We could use lambda in Java8
//                        new CreateFunction<Integer>() {
//                            @Override
//                            public Integer create(CreationalContext<Integer> creationalContext) {
//                                return SEQUENCE.incrementAndGet();
//                            }
//                        })
//                .build();
    }

    static Bean<?> createIdSupplierBean() {
        return MockBean.<IdSupplier>builder()
            .types(IdSupplier.class)
            .scope(ApplicationScoped.class)
            .create(new CreateFunction<IdSupplier>() {
                @Override
                public IdSupplier create(CreationalContext<IdSupplier> creationalContext) {
                    return new IdSupplier(UUID.randomUUID().toString());
                }
            }).build();
    }

    @Test
    public void testBeansAdded() {
        // Blue injects @Meaty List<String>
        Assertions.assertEquals("42", weld.select(Blue.class).get().getStringList().get(0));

        // Each Bean.create() increments the sequence
        SEQUENCE.set(0);
        for (int i = 1; i < 11; i++) {
            Assertions.assertEquals(Integer.valueOf(i),
                weld.select(Integer.class, Meaty.Literal.INSTANCE).get());
        }

        // Mock with default settings
        MyService myService = weld.select(MyService.class).get();
        myService.doBusiness("Adalbert");
        Mockito.verify(myService, Mockito.atLeastOnce()).doBusiness(ArgumentMatchers.anyString());

        // Test applicaction scoped bean
        Assertions.assertEquals(weld.select(IdSupplier.class).get().getId(), weld.select(IdSupplier.class).get().getId());
    }

    interface MyService {

        void doBusiness(String name);

    }

    static class IdSupplier {

        private final String id;

        public IdSupplier(String id) {
            this.id = id;
        }

        String getId() {
            return id;
        }

    }

}
