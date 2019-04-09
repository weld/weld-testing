package org.jboss.weld.junit5.bean;

import org.jboss.weld.junit.MockBeanWithPriority;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@ExtendWith(WeldJunit5Extension.class)
public class AddGloballyEnabledAlternativeTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Bar.class)
            .addBeans(createFooAlternativeBean(), createListBean())
            .addBeans(MockBeanWithPriority.of(Mockito.mock(MyService.class), MyService.class))
            .build();

    public static Bean<?> createFooAlternativeBean(){
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
        Assertions.assertNotNull(bar.getFoo());
        Assertions.assertNotNull(bar.getSomeList());
        Assertions.assertEquals("42", bar.getSomeList().get(0));

        // Mock with default settings
        MyService myService = weld.select(MyService.class).get();
        myService.doBusiness("Adalbert");
        Mockito.verify(myService, Mockito.atLeastOnce()).doBusiness(ArgumentMatchers.anyString());

        // assert all of these are actually alternatives (enabled)
        Set<Bean<?>> beans = weld.getBeanManager().getBeans(Foo.class, Meaty.Literal.INSTANCE);
        Assertions.assertEquals(1, beans.size());
        Assertions.assertTrue(beans.iterator().next().isAlternative());

        beans = weld.getBeanManager().getBeans(MyService.class);
        Assertions.assertEquals(1, beans.size());
        Assertions.assertTrue(beans.iterator().next().isAlternative());

        beans = weld.getBeanManager().getBeans(new TypeLiteral<List<String>>(){}.getType());
        Assertions.assertEquals(1, beans.size());
        Assertions.assertTrue(beans.iterator().next().isAlternative());
    }

    interface MyService {

        void doBusiness(String name);

    }
}
