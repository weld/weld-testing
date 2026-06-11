/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.junit.jupiter.explicitInjection;

import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldJunit5Extension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Verifies that new property=false overrides legacy property=true,
 * resulting in explicit parameter injection being disabled (Weld resolves greedily).
 */
@Isolated
@EnableWeld
public class ExplicitParameterInjectionLegacyPropertyIgnoredTest {

    @BeforeAll
    public static void prepare() {
        System.setProperty(WeldJunit5Extension.GLOBAL_EXPLICIT_PARAM_INJECTION, "false");
        System.setProperty("org.jboss.weld.junit5.explicitParamInjection", "true");
    }

    @Test
    public void testNewPropertyTakesPrecedence(Foo foo) {
        // Foo is injected without any qualifier; this only works if explicit injection is OFF (greedy mode).
        // If explicit injection were ON, Weld would refuse to resolve unqualified parameters
        // and JUnit would throw ParameterResolutionException.
        Assertions.assertNotNull(foo);
        Assertions.assertEquals(Foo.class.getSimpleName(), foo.ping());
    }

    @AfterAll
    public static void cleanUp() {
        System.clearProperty(WeldJunit5Extension.GLOBAL_EXPLICIT_PARAM_INJECTION);
        System.clearProperty("org.jboss.weld.junit5.explicitParamInjection");
    }
}
