package org.jboss.weld.junit5.auto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.junit5.auto.discovery.WithBeanDefiningAnnotation;
import org.jboss.weld.junit5.auto.discovery.WithoutBeanDefiningAnnotation;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddPackages(value = WithBeanDefiningAnnotation.class, recursively = false)
@SetBeanDiscoveryMode(BeanDiscoveryMode.ALL)
public class SetDiscoveryModeAllTest {

    @Inject
    private WithBeanDefiningAnnotation standardBean;

    @Inject
    Instance<Object> instance;

    @Test
    void testDiscoveryModeWasChanged() {
        // bean with bean defining annotation is normally resolvable
        assertNotNull(standardBean);
        // bean without bean defining annotation is discovered as well because we set discovery to ALL
        assertTrue(instance.select(WithoutBeanDefiningAnnotation.class).isResolvable());
    }
}
