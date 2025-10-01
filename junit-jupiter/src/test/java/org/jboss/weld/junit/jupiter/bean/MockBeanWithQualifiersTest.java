package org.jboss.weld.junit.jupiter.bean;

import jakarta.enterprise.inject.Any;

import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.jboss.weld.testing.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link MockBean} with custom qualifiers has {@link Any} qualifier automatically added.
 */
@EnableWeld
public class MockBeanWithQualifiersTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(AlternativeMockBeanTest.SimpleService.class)
            .addBeans(MockBean.builder().types(String.class).qualifiers(Meaty.Literal.INSTANCE).create(c -> "foo").build())
            .build();

    @Test
    public void testBeanHasDefaultQualifiersAdded() {
        Assertions.assertEquals("foo", weld.select(String.class, Any.Literal.INSTANCE).get());
    }
}
