package org.jboss.weld.junit5.basic;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
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
