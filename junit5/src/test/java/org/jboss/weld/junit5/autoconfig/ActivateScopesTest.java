package org.jboss.weld.junit5.autoconfig;


import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;

import org.jboss.weld.junit5.ActivateScopes;
import org.jboss.weld.junit5.EnableAlternative;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.autoconfig.beans.Engine;
import org.jboss.weld.junit5.autoconfig.beans.V6;
import org.jboss.weld.junit5.autoconfig.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



@EnableWeld
@ActivateScopes({ SessionScoped.class, RequestScoped.class })
public class ActivateScopesTest {

  @Produces
  @SessionScoped
  @Named("special")
  V8 sessionEngine = new V8();

  @Produces
  @ConversationScoped
  @EnableAlternative // V8 is annotated with @ApplicationScoped, this tells the container to use this ConversationScoped bean instead
  V6 convoEngine = new V6();

  @Test
  @DisplayName("Test that @ActivateScopes activates the specified scopes")
  void test(BeanManager beanManager) {
    assertTrue(beanManager.getContext(RequestScoped.class).isActive());
    assertTrue(beanManager.getContext(SessionScoped.class).isActive());
  }

  @Test
  @DisplayName("Test that Engine is resolved to @SessionScoped V8")
  void test(@Named("special") Engine engine) {
    assertEquals(engine.getThrottle(), 0);
  }

  @Test
  @DisplayName("Test that non-activated scopes fail")
  void test(V6 engine) {
    assertThrows(ContextNotActiveException.class, engine::getThrottle);
  }

}
