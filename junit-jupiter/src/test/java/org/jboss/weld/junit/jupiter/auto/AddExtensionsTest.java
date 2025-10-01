package org.jboss.weld.junit.jupiter.auto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.weld.junit.jupiter.auto.extension.AddedExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddExtensions(AddedExtension.class)
public class AddExtensionsTest {

    @Test
    @DisplayName("Test that @AddExtensions adds the specified extensions")
    void test() {
        assertTrue(AddedExtension.isEnabled());
    }

}
