package org.jboss.weld.junit4.bean;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests {@link org.jboss.weld.junit.MockBean} adding a bean that is a globally enabled alternative.
 *
 * @author Matej Novotny
 */
public class AddGloballyEnabledAlternativeTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.from(Bar.class)
            .addBeans(createFooAlternativeBean(), createListBean())
            .build();

    public static Bean<?> createFooAlternativeBean() {
        return MockBean.read(Foo.class)
                .priority(3)
                .alternative(true)
                .addQualifier(Meaty.Literal.INSTANCE)
                .build();
    }

    static Bean<?> createListBean() {
        return MockBean.builder()
                .types(new TypeLiteral<List<String>>() {
                }.getType())
                .globallySelectedAlternative(2)
                .creating(
                        // Mock object provided by Mockito
                        Mockito.when(Mockito.mock(List.class).get(0)).thenReturn("42").getMock())
                .build();
    }

    @Test
    public void testAllBeansAreAddedAndCanBeSelected() {
        Bar bar = weld.select(Bar.class).get();
        Assert.assertNotNull(bar.getFoo());
        Assert.assertNotNull(bar.getSomeList());
        Assert.assertEquals("42", bar.getSomeList().get(0));

        // assert all of these are actually alternatives (enabled)
        Set<Bean<?>> beans = weld.getBeanManager().getBeans(Foo.class, Meaty.Literal.INSTANCE);
        Assert.assertEquals(1, beans.size());
        Assert.assertTrue(beans.iterator().next().isAlternative());

        beans = weld.getBeanManager().getBeans(new TypeLiteral<List<String>>() {
        }.getType());
        Assert.assertEquals(1, beans.size());
        Assert.assertTrue(beans.iterator().next().isAlternative());
    }
}
