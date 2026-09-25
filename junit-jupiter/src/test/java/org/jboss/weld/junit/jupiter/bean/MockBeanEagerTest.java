/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.junit.jupiter.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@EnableWeld
class MockBeanEagerTest {
    private final AtomicInteger eagerCreations = new AtomicInteger();
    private final AtomicInteger prioritizedEagerCreations = new AtomicInteger();
    private final AtomicInteger lazyCreations = new AtomicInteger();
    private final AtomicInteger prioritizedLazyCreations = new AtomicInteger();

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(ServiceConsumer.class)
            .addBeans(service("eager", true, false, eagerCreations),
                    service("prioritized-eager", true, true, prioritizedEagerCreations),
                    service("lazy", false, false, lazyCreations),
                    service("prioritized-lazy", false, true, prioritizedLazyCreations))
            .build();

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void eagerBeanIsCreatedBeforeTestBegins(boolean prioritized) {
        AtomicInteger creations = prioritized ? prioritizedEagerCreations : eagerCreations;
        String name = prioritized ? "prioritized-eager" : "eager";
        assertEquals(1, creations.get());
        assertEquals("ready", weld.select(ServiceConsumer.class).get().value(name));
        assertEquals(1, creations.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void lazyBeanIsCreatedOnFirstUse(boolean prioritized) {
        AtomicInteger creations = prioritized ? prioritizedLazyCreations : lazyCreations;
        String name = prioritized ? "prioritized-lazy" : "lazy";
        assertEquals(0, creations.get());
        assertEquals("ready", weld.select(ServiceConsumer.class).get().value(name));
        assertEquals(1, creations.get());
    }

    private static MockBean<Service> service(String name, boolean eager, boolean prioritized, AtomicInteger creations) {
        MockBean.Builder<Service> builder = MockBean.<Service> builder()
                .types(Service.class).qualifiers(NamedLiteral.of(name)).scope(ApplicationScoped.class).eager(eager)
                .create(ctx -> {
                    creations.incrementAndGet();
                    return new Service();
                });
        if (prioritized) {
            builder.priority(10);
        }
        return builder.build();
    }

    public static class ServiceConsumer {
        @Inject
        @Any
        Instance<Service> services;

        public String value(String name) {
            return services.select(NamedLiteral.of(name)).get().value();
        }
    }

    public static class Service {
        public String value() {
            return "ready";
        }
    }
}
