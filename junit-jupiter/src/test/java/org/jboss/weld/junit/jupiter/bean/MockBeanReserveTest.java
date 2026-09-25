/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.junit.jupiter.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MockBeanReserveTest {

    @Nested
    @EnableWeld
    class WithoutRegularBean {
        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(ServiceConsumer.class)
                .addBeans(MockBean.<Service> builder().types(Service.class)
                        .reserve(true).priority(10).creating(() -> "fallback").build())
                .build();

        @Test
        void selectsReserve() {
            assertEquals("fallback", weld.select(ServiceConsumer.class).get().services.get().value());
        }
    }

    @Nested
    @EnableWeld
    class WithRegularBean {
        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(ServiceConsumer.class)
                .addBeans(MockBean.<Service> builder().types(Service.class)
                        .reserve(true).priority(10).creating(() -> "fallback").build(),
                        MockBean.<Service> of(() -> "regular", Service.class))
                .build();

        @Test
        void selectsRegularBean() {
            assertEquals("regular", weld.select(ServiceConsumer.class).get().services.get().value());
        }
    }

    @Nested
    @EnableWeld
    class CompetingReserves {
        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(ServiceConsumer.class)
                .addBeans(MockBean.<Service> builder().types(Service.class).beanClass(FirstService.class)
                        .reserve(true).priority(10).creating(new FirstService()).build(),
                        MockBean.<Service> builder().types(Service.class).beanClass(SecondService.class)
                                .reserve(true).priority(20).creating(new SecondService()).build())
                .build();

        @Test
        void selectsHigherPriorityReserve() {
            assertEquals("second", weld.select(ServiceConsumer.class).get().services.get().value());
        }
    }

    @Nested
    @EnableWeld
    class WithoutPriority {
        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(ServiceConsumer.class)
                .addBeans(MockBean.<Service> builder().types(Service.class)
                        .reserve(true).creating(() -> "fallback").build())
                .build();

        @Test
        void doesNotSelectReserve() {
            assertTrue(weld.select(ServiceConsumer.class).get().services.isUnsatisfied());
        }
    }

    public static class ServiceConsumer {
        @Inject
        Instance<Service> services;
    }

    interface Service {
        String value();
    }

    static class FirstService implements Service {
        @Override
        public String value() {
            return "first";
        }
    }

    static class SecondService implements Service {
        @Override
        public String value() {
            return "second";
        }
    }
}
