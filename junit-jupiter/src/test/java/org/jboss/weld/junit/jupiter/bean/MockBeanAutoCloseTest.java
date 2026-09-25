/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.junit.jupiter.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Instance.Handle;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@EnableWeld
class MockBeanAutoCloseTest {
    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(ResourceConsumer.class)
            .addBeans(resource("close", true, false), resource("prioritized-close", true, true),
                    resource("no-close", false, false), resource("prioritized-no-close", false, true))
            .build();

    @ParameterizedTest
    @CsvSource({ "close,true", "prioritized-close,true", "no-close,false", "prioritized-no-close,false" })
    void destroyingDependentInstanceHonorsAutoClose(String name, boolean autoClose) {
        Handle<Resource> handle = weld.select(ResourceConsumer.class).get().resources.select(NamedLiteral.of(name)).getHandle();
        Resource instance = handle.get();
        assertTrue(instance.events.isEmpty());

        handle.destroy();

        assertEquals(autoClose ? List.of("destroy", "close") : List.of("destroy"), instance.events);
    }

    private static MockBean<Resource> resource(String name, boolean autoClose, boolean prioritized) {
        MockBean.Builder<Resource> builder = MockBean.<Resource> builder().types(Resource.class)
                .qualifiers(NamedLiteral.of(name)).autoClose(autoClose).create(ctx -> new Resource())
                .destroy((instance, ctx) -> instance.events.add("destroy"));
        if (prioritized) {
            builder.priority(10);
        }
        return builder.build();
    }

    public static class ResourceConsumer {
        @Inject
        @Any
        Instance<Resource> resources;
    }

    public static class Resource implements AutoCloseable {
        private final List<String> events = new ArrayList<>();

        @Override
        public void close() {
            events.add("close");
        }
    }
}
