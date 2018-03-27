package org.jboss.weld.junit5.autoconfig;

import javax.enterprise.inject.Produces;

import org.jboss.weld.junit5.AddClasses;
import org.jboss.weld.junit5.EnableAlternative;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.basic.Foo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;



@EnableWeld
@AddClasses(Foo.class)
class ProducesAlternativeTest {

  @Produces
  @EnableAlternative
  Foo fakeFoo = new Foo("non-baz");

  @Test
  @DisplayName("Ensure @Produces/@EnableAlternative overrides the @ApplicationScoped bean defined by the Foo class")
  void test(Foo myFoo) {
    assertNotNull(myFoo);
    assertEquals(myFoo.getBar(), "non-baz");
  }

}
