/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.junit.jupiter.explicitInjection;

import jakarta.enterprise.inject.Default;

import org.jboss.weld.junit.jupiter.EnableWeld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Verifies that the legacy system property name ({@code org.jboss.weld.junit5.explicitParamInjection})
 * is still accepted for backward compatibility.
 */
@Isolated
@EnableWeld
public class ExplicitParameterInjectionViaLegacyPropertyTest {

    @BeforeAll
    public static void prepare() {
        System.setProperty("org.jboss.weld.junit5.explicitParamInjection", "true");
    }

    @Test
    @ExtendWith(CustomExtension.class)
    public void testParametersNeedExtraAnnotation(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
        // Bar should be resolved by another extension
        Assertions.assertNotNull(bar);
        Assertions.assertEquals(CustomExtension.class.getSimpleName(), bar.ping());
        // Foo should be resolved as usual
        Assertions.assertNotNull(foo);
        Assertions.assertEquals(Foo.class.getSimpleName(), foo.ping());
        // BeanWithQualifier should be resolved
        Assertions.assertNotNull(bean);
        Assertions.assertEquals(BeanWithQualifier.class.getSimpleName(), bean.ping());
    }

    @AfterAll
    public static void cleanUp() {
        System.clearProperty("org.jboss.weld.junit5.explicitParamInjection");
    }
}
