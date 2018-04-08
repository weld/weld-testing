package org.jboss.weld.junit5.auto;


import javax.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.auto.beans.Engine;
import org.jboss.weld.junit5.auto.beans.V8;
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
