package org.jboss.weld.junit.jupiter.explicitInjection;

import jakarta.enterprise.inject.Default;

import org.jboss.weld.junit.jupiter.ExplicitParamInjection;
import org.jboss.weld.junit.jupiter.WeldJupiterExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WeldJupiterExtension.class)
@ExplicitParamInjection(false)
public class ExplicitParameterInjectionNestedClass2Test {

    @Test
    public void testParameterResolution(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
        // Bar should be resolved by Weld
        Assertions.assertNotNull(bar);
        Assertions.assertEquals(Bar.class.getSimpleName(), bar.ping());
        // Foo should be resolved as usual
        Assertions.assertNotNull(foo);
        Assertions.assertEquals(Foo.class.getSimpleName(), foo.ping());
        // BeanWithQualifier should be resolved
        Assertions.assertNotNull(bean);
        Assertions.assertEquals(BeanWithQualifier.class.getSimpleName(), bean.ping());
    }

    @Nested
    class NestedTestClass {

        @Test
        public void testParameterResolution(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
            // Bar should be resolved by Weld
            Assertions.assertNotNull(bar);
            Assertions.assertEquals(Bar.class.getSimpleName(), bar.ping());
            // Foo should be resolved as usual
            Assertions.assertNotNull(foo);
            Assertions.assertEquals(Foo.class.getSimpleName(), foo.ping());
            // BeanWithQualifier should be resolved
            Assertions.assertNotNull(bean);
            Assertions.assertEquals(BeanWithQualifier.class.getSimpleName(), bean.ping());
        }

        @Nested
        class TwiceNestedTestClass1 {

            @Test
            public void testParameterResolution(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
                // Bar should be resolved by Weld
                Assertions.assertNotNull(bar);
                Assertions.assertEquals(Bar.class.getSimpleName(), bar.ping());
                // Foo should be resolved as usual
                Assertions.assertNotNull(foo);
                Assertions.assertEquals(Foo.class.getSimpleName(), foo.ping());
                // BeanWithQualifier should be resolved
                Assertions.assertNotNull(bean);
                Assertions.assertEquals(BeanWithQualifier.class.getSimpleName(), bean.ping());
            }
        }

        @Nested
        @ExplicitParamInjection(true)
        @ExtendWith(CustomExtension.class) // TwiceNestedTestClass2 and ThriceNestedClass will both use this
        class TwiceNestedTestClass2 {

            @Test
            public void testParameterResolution(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
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

            @Nested
            class ThriceNestedClass {

                @Test
                public void testParameterResolution(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
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
            }
        }
    }
}
