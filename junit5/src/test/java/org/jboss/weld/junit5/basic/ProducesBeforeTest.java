package org.jboss.weld.junit5.basic;


import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.jboss.weld.junit5.WeldJunit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;



@ExtendWith(WeldJunit5Extension.class)
public class ProducesBeforeTest {

  private Integer requiredValue;

  @BeforeEach
  void init() {
    requiredValue = 10;
  }

  @Produces
  @Dependent
  @Named("computed")
  Integer provideComputedValue() {
    return requiredValue * 10;
  }

  @Test
  void testIt(@Named("computed") Integer computed) {
    assertNotNull(computed);
  }

}
