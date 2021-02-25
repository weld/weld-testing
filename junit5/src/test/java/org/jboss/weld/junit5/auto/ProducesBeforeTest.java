package org.jboss.weld.junit5.auto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

import static org.junit.jupiter.api.Assertions.assertNotNull;



@EnableAutoWeld
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
