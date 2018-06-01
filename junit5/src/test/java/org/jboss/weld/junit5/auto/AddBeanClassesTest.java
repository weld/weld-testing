package org.jboss.weld.junit5.auto;


import org.jboss.weld.junit5.auto.beans.Engine;
import org.jboss.weld.junit5.auto.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;



@EnableAutoWeld
@AddBeanClasses(V8.class)
public class AddBeanClassesTest {

  @Inject
  private Engine engine;

  @Test
  @DisplayName("Test that @AddBeanClasses pulls in V8 to fulfill the injected Engine interface")
  void test() {
    assertNotNull(engine);
  }

}
