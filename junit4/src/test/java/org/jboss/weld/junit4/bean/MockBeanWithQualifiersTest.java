package org.jboss.weld.junit4.bean;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import jakarta.enterprise.inject.Any;

/**
 * Tests that {@link MockBean} with custom qualifiers has {@link Any} qualifier automatically added.
 */
public class MockBeanWithQualifiersTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.from(MockBeanWithQualifiersTest.class)
            .addBeans(MockBean.builder().types(String.class).qualifiers(Meaty.Literal.INSTANCE).create(c -> "foo").build())
            .build();

    @Test
    public void testBeanHasDefaultQualifiersAdded() {
        Assert.assertEquals("foo", weld.select(String.class, Any.Literal.INSTANCE).get());
    }
}
