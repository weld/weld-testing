package org.jboss.weld.junit5.auto;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;



@EnableWeld
class ProducesBeforeTest {

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
  @DisplayName("Ensure that @BeforeEach runs before any @Produces methods/fields are interrogated")
  void test(@Named("computed") Integer computed) {
    assertNotNull(computed);
  }

}
