package org.jboss.weld.junit.jupiter.alternative;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;

import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.jboss.weld.testing.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests that synthetic archive with only enabled alternative in it still works.
 * This mimics problems in issue 64, but without the need for discovery
 */
@EnableWeld
public class AlternativeAsSoleBeanInSyntheticArchiveTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld())
            .addBeans(createSelectedAlternativeBean()).build();

    static Bean<?> createSelectedAlternativeBean() {
        return MockBean.builder().types(Fish.class).scope(ApplicationScoped.class).selectedAlternative(Fish.class)
                .creating(new Fish(200)).build();
    }

    @Inject
    Fish fish;

    @Test
    public void testAlternativeCanBeUsed() {
        Assertions.assertTrue(fish.getNumberOfLegs() == 200);
    }
}
