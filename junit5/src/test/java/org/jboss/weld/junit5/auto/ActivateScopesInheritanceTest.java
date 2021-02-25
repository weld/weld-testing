package org.jboss.weld.junit5.auto;

import org.jboss.weld.junit5.auto.beans.Engine;
import org.jboss.weld.junit5.auto.beans.V6;
import org.jboss.weld.junit5.auto.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Named;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivateScopesInheritanceTest extends BaseActivateScopesInheritanceTest {

    @Produces
    @SessionScoped
    @Named("special")
    V8 sessionEngine = new V8();

    @Produces
    @ConversationScoped
    @ExcludeBean // V6 is annotated with @ApplicationScoped, this tells the container to use producer instead
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

/**
 * Base class for {@link ActivateScopesInheritanceTest} to test if annotations are inherited.
 */
@EnableAutoWeld
@ActivateScopes({ SessionScoped.class, RequestScoped.class })
class BaseActivateScopesInheritanceTest {
    // empty, just a base class to let annotations be inherited
}
