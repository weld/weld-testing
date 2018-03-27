package org.jboss.weld.junit5.autoconfig;


import javax.inject.Inject;

import org.jboss.weld.junit5.AddPackages;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.autoconfig.beans.Engine;
import org.jboss.weld.junit5.autoconfig.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;



@EnableWeld
@AddPackages(Engine.class)
public class AddPackagesTest {

  @Inject
  private V8 engine;

  @Test
  @DisplayName("Test that @AddPackages pulls in V8 to fulfill the injected Engine interface")
  void test() {
    assertNotNull(engine);
  }

}
