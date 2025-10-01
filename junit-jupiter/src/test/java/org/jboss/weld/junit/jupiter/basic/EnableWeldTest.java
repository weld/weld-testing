package org.jboss.weld.junit.jupiter.basic;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.weld.junit.jupiter.EnableWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableWeld
public class EnableWeldTest {

    @Inject
    BeanManager beanManager;

    @Test
    @DisplayName("@EnableWeld initializes the Weld CDI container")
    void enableWeldAnnotationInitializesTheWeldContainer() {
        assertNotNull(beanManager);
    }

}
