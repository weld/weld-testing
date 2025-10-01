package org.jboss.weld.junit.jupiter.initiator.discovery;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldJUnitJupiterExtension;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.jboss.weld.junit.jupiter.initiator.bean.Foo;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WeldJUnitJupiterExtension.class)
public abstract class SuperclassWithWeldInitiator {
    @WeldSetup
    public WeldInitiator weld = WeldInitiator
            .of(new Weld()
                    .addBeanClass(Foo.class));
}
