package org.jboss.weld.junit4.bean;

import org.jboss.weld.junit.MockBeanWithPriority;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;
import java.util.List;
import java.util.Set;

/**
 * Tests {@link MockBeanWithPriority} which adds a bean that is always an enabled alternative.
 *
 * @author Matej Novotny
 */
public class AddGloballyEnabledAlternativeTest {
    
    @Rule
    public WeldInitiator weld = WeldInitiator.from(Bar.class)
            .addBeans(createFooAlternativeBean(), createListBean())
            .addBeans(MockBeanWithPriority.of(Mockito.mock(MyService.class), MyService.class))
            .build();

    public static Bean<?> createFooAlternativeBean() {
        return MockBeanWithPriority.read(Foo.class)
                .priority(3)
                .addQualifier(Meaty.Literal.INSTANCE)
                .build();
    }

    static Bean<?> createListBean() {
        return MockBeanWithPriority.builder()
                .types(new TypeLiteral<List<String>>() {
                }.getType())
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

        // Mock with default settings
        MyService myService = weld.select(MyService.class).get();
        myService.doBusiness("Adalbert");
        Mockito.verify(myService, Mockito.atLeastOnce()).doBusiness(ArgumentMatchers.anyString());

        // assert all of these are actually alternatives (enabled)
        Set<Bean<?>> beans = weld.getBeanManager().getBeans(Foo.class, Meaty.Literal.INSTANCE);
        Assert.assertEquals(1, beans.size());
        Assert.assertTrue(beans.iterator().next().isAlternative());

        beans = weld.getBeanManager().getBeans(MyService.class);
        Assert.assertEquals(1, beans.size());
        Assert.assertTrue(beans.iterator().next().isAlternative());

        beans = weld.getBeanManager().getBeans(new TypeLiteral<List<String>>() {
        }.getType());
        Assert.assertEquals(1, beans.size());
        Assert.assertTrue(beans.iterator().next().isAlternative());
    }

    interface MyService {

        void doBusiness(String name);

    }
}
