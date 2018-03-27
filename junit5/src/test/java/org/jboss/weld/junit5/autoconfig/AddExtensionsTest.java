package org.jboss.weld.junit5.autoconfig;


import org.jboss.weld.junit5.AddExtensions;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.autoconfig.extension.AddedExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;



@EnableWeld
@AddExtensions(AddedExtension.class)
public class AddExtensionsTest {

  @Test
  @DisplayName("Test that @AddExtensions adds the specified extensions")
  void test() {
    assertTrue(AddedExtension.isEnabled());
  }

}
