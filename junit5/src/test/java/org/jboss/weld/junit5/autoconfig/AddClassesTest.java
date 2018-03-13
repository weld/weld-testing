package org.jboss.weld.junit5.autoconfig;


import javax.inject.Inject;

import org.jboss.weld.junit5.AddClasses;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.autoconfig.beans.Engine;
import org.jboss.weld.junit5.autoconfig.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;



@EnableWeld
@AddClasses(V8.class)
public class AddClassesTest {

  @Inject
  private Engine engine;

  @Test
  @DisplayName("Test that @AddClasses pulls in V8 to fulfill the injected Engine interface")
  void test() {
    assertNotNull(engine);
  }

}
