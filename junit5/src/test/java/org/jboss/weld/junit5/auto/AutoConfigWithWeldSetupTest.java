package org.jboss.weld.junit5.auto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Optional;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/**
 * NOTE - this test is one massive hack because JUnit, at the time of writing, doesn't allow to assert that an
 * exception was throws inside an extension. The test is fragile and can break because of internal JUnit 5 changes
 * in which case we might want to revisit if we want to keep it or whether there is a better way.
 *
 * @author Björn Kautler
 */
@EnableAutoWeld
class AutoConfigWithWeldSetupTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(WeldInitiator.createWeld());

    @Test
    @ExtendWith(AssertExceptionExtension.class)
    @DisplayName("Ensure that @WeldSetup is not compatible with automagic mode")
    void test() {
        fail("IllegalStateException expected before test method is entered");
    }

    /**
     * @author Björn Kautler
     */
    static public class AssertExceptionExtension implements AfterEachCallback {

        @Override
        public void afterEach(ExtensionContext context) throws NoSuchFieldException, IllegalAccessException {
            Optional<Throwable> executionException = context.getExecutionException();
            if (executionException.isPresent()) {
                Field throwableCollectorField = context.getClass().getDeclaredField("throwableCollector");
                throwableCollectorField.setAccessible(true);
                ThrowableCollector throwableCollector = (ThrowableCollector) throwableCollectorField.get(context);
                Field throwableField = ThrowableCollector.class.getDeclaredField("throwable");
                throwableField.setAccessible(true);
                throwableField.set(throwableCollector, null);

                Throwable throwable = executionException.get();
                assertInstanceOf(IllegalStateException.class, throwable);
                assertEquals("When using automagic mode, no @WeldSetup annotated field should be present! Fields found:\n"
                        + "Field 'weld' with type class org.jboss.weld.junit5.WeldInitiator which is in class org.jboss.weld.junit5.auto.AutoConfigWithWeldSetupTest",
                        throwable.getMessage());
            }
        }
    }
}
