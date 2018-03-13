package org.jboss.weld.junit5.basic;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import org.jboss.weld.junit5.EnableAlternative;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;



@ExtendWith(WeldJunit5Extension.class)
public class ProducesAlternativeTest {

  @Produces
  @Alternative
  @EnableAlternative
  Foo fakeFoo = new Foo("non-baz");

  @Test
  void testIt(Foo myFoo) {
    assertNotNull(myFoo);
    assertEquals(myFoo.getBar(), "non-baz");
  }

}
