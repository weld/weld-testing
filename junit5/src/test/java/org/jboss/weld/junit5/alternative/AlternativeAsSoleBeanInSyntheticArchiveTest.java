package org.jboss.weld.junit5.alternative;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;

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
