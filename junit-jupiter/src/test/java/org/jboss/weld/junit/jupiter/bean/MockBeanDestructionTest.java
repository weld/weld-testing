/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.junit.jupiter.bean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.junit.MockBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MockBeanDestructionTest {

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void failedDestructionStillClosesAndReleasesContext(boolean failingCallback) {
        List<String> events = new ArrayList<>();
        AutoCloseable service = () -> {
            events.add("close");
            throw new Exception("Expected close failure");
        };
        MockBean<AutoCloseable> bean = MockBean.<AutoCloseable> builder().autoClose(true).creating(service)
                .destroy((instance, ctx) -> {
                    events.add("destroy");
                    if (failingCallback) {
                        throw new IllegalStateException("Expected destroy failure");
                    }
                }).build();
        CreationalContext<AutoCloseable> context = new CreationalContext<>() {
            @Override
            public void push(AutoCloseable incompleteInstance) {
            }

            @Override
            public void release() {
                events.add("release");
            }
        };

        assertDoesNotThrow(() -> bean.destroy(service, context));
        assertEquals(List.of("destroy", "close", "release"), events);
    }

    @Test
    void autoCloseIgnoresInstancesThatAreNotAutoCloseable() {
        AtomicInteger releases = new AtomicInteger();
        Object instance = new Object();
        MockBean<Object> bean = MockBean.builder().autoClose(true).creating(instance).build();
        CreationalContext<Object> context = new CreationalContext<>() {
            @Override
            public void push(Object incompleteInstance) {
            }

            @Override
            public void release() {
                releases.incrementAndGet();
            }
        };
        assertDoesNotThrow(() -> bean.destroy(instance, context));
        assertEquals(1, releases.get());
    }

}
