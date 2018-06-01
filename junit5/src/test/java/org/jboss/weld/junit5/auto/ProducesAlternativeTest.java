package org.jboss.weld.junit5.auto;

import org.jboss.weld.junit5.basic.Foo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;



@EnableAutoWeld
@AddBeanClasses(Foo.class)
class ProducesAlternativeTest {

  @Produces
  @OverrideBean
  Foo fakeFoo = new Foo("non-baz");

  @Test
  @DisplayName("Ensure @Produces/@OverrideBean overrides the @ApplicationScoped bean defined by the Foo class")
  void test(Foo myFoo) {
    assertNotNull(myFoo);
    assertEquals(myFoo.getBar(), "non-baz");
  }

}
