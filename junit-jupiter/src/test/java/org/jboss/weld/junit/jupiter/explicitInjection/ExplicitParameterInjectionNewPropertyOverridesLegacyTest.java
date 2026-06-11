/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.junit.jupiter.explicitInjection;

import jakarta.enterprise.inject.Default;

import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldJunit5Extension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Verifies that new property=true overrides legacy property=false,
 * resulting in explicit parameter injection being enabled.
 */
@Isolated
@EnableWeld
public class ExplicitParameterInjectionNewPropertyOverridesLegacyTest {

    @BeforeAll
    public static void prepare() {
        System.setProperty(WeldJunit5Extension.GLOBAL_EXPLICIT_PARAM_INJECTION, "true");
        System.setProperty("org.jboss.weld.junit5.explicitParamInjection", "false");
    }

    @Test
    @ExtendWith(CustomExtension.class)
    public void testNewPropertyTakesPrecedence(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
        // explicit injection is ON (new property wins), so Bar is resolved by CustomExtension
        Assertions.assertNotNull(bar);
        Assertions.assertEquals(CustomExtension.class.getSimpleName(), bar.ping());
        // Foo needs @Default qualifier to be resolved by Weld
        Assertions.assertNotNull(foo);
        Assertions.assertEquals(Foo.class.getSimpleName(), foo.ping());
        // BeanWithQualifier resolved by Weld via qualifier
        Assertions.assertNotNull(bean);
        Assertions.assertEquals(BeanWithQualifier.class.getSimpleName(), bean.ping());
    }

    @AfterAll
    public static void cleanUp() {
        System.clearProperty(WeldJunit5Extension.GLOBAL_EXPLICIT_PARAM_INJECTION);
        System.clearProperty("org.jboss.weld.junit5.explicitParamInjection");
    }
}
