package org.jboss.weld.junit.jupiter.initiator.discovery;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jboss.weld.junit.jupiter.initiator.bean.Foo;
import org.junit.jupiter.api.Test;

public class SubclassWithWeldInitiatorTest extends SuperclassWithWeldInitiator {
    @Test
    public void testSuperclassWeldInit() {
        final Foo foo = weld.select(Foo.class).get();
        assertNotNull(foo);
    }
}
