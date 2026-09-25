/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.junit.jupiter.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Eager;
import jakarta.enterprise.inject.Instance.Handle;
import jakarta.enterprise.inject.Reserve;
import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Inject;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// MockBean.read() uses Unmanaged and WeldContainer.current(), which requires a single running container.
@Isolated
@EnableWeld
class MockBeanReadTest {
    @Inject
    LifecycleTracker tracker;
    private final MockBean<OverriddenService> overridden = MockBean.read(OverriddenService.class)
            .reserve(false).eager(false).autoClose(false).build();

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(LifecycleTracker.class)
            .addBeans(MockBean.read(AnnotatedService.class).priority(10).build(),
                    MockBean.read(StereotypedService.class).priority(10).build(), overridden)
            .inject(this).build();

    @ParameterizedTest
    @ValueSource(classes = { AnnotatedService.class, StereotypedService.class })
    void readsAttributesAndClosesUnmanagedInstanceOnce(Class<? extends LifecycleService> type) {
        Handle<? extends LifecycleService> handle = weld.select(type).getHandle();
        assertTrue(handle.getBean().isReserve());
        assertTrue(handle.getBean().isEager());
        assertTrue(handle.getBean().isAutoClose());
        assertEquals(List.of("postConstruct"), tracker.eventsFor(type));

        handle.get();
        handle.destroy();

        assertEquals(List.of("postConstruct", "preDestroy", "close"), tracker.eventsFor(type));
    }

    @Test
    void explicitSettingsOverrideReadAttributes() {
        assertFalse(overridden.isReserve());
        assertFalse(overridden.isEager());
        assertFalse(overridden.isAutoClose());
        assertTrue(tracker.eventsFor(OverriddenService.class).isEmpty());

        Handle<OverriddenService> handle = weld.select(OverriddenService.class).getHandle();
        assertEquals("ready", handle.get().value());
        handle.destroy();

        assertEquals(List.of("postConstruct", "preDestroy"), tracker.eventsFor(OverriddenService.class));
    }

    @ApplicationScoped
    public static class LifecycleTracker {
        private final Map<Class<?>, List<String>> events = new HashMap<>();

        public List<String> eventsFor(Class<?> type) {
            return events.computeIfAbsent(type, ignored -> new ArrayList<>());
        }
    }

    public static class LifecycleService implements AutoCloseable {
        @Inject
        LifecycleTracker tracker;

        public String value() {
            return "ready";
        }

        @PostConstruct
        public void postConstruct() {
            tracker.eventsFor(getClass()).add("postConstruct");
        }

        @PreDestroy
        public void preDestroy() {
            tracker.eventsFor(getClass()).add("preDestroy");
        }

        @Override
        public void close() {
            tracker.eventsFor(getClass()).add("close");
        }
    }

    @ApplicationScoped
    @Reserve
    @Eager
    @AutoClose
    public static class AnnotatedService extends LifecycleService {
    }

    @NestedResourceStereotype
    public static class StereotypedService extends LifecycleService {
    }

    @ResourceStereotype
    public static class OverriddenService extends LifecycleService {
    }

    @Stereotype
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @ApplicationScoped
    @Reserve
    @Eager
    @AutoClose
    @interface ResourceStereotype {
    }

    @Stereotype
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @ResourceStereotype
    @interface NestedResourceStereotype {
    }
}
