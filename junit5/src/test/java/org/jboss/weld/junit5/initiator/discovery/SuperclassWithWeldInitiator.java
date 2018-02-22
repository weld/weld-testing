package org.jboss.weld.junit5.initiator.discovery;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.junit5.initiator.bean.Foo;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WeldJunit5Extension.class)
public abstract class SuperclassWithWeldInitiator
{
	@WeldSetup
	public WeldInitiator weld = WeldInitiator
			.of(new Weld()
					.addBeanClass(Foo.class));
}
