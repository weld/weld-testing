package org.jboss.weld.junit5;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Supplier;

import javax.enterprise.inject.Instance;

public class WeldAssertions {

    private WeldAssertions() {
    }

    public static void assertUnresolvable(Instance<?> instance) {
        assertUnresolvable(instance, () -> null);
    }

    public static void assertUnresolvable(Instance<?> instance, Supplier<String> messageSupplier) {
        if (isResolvable(instance)) {
            fail(messageSupplier);
        }
    }

    public static void assertResolvable(Instance<?> instance) {
        assertResolvable(instance, () -> null);
    }

    public static void assertResolvable(Instance<?> instance, Supplier<String> messageSupplier) {
        if (isUnresolvable(instance)) {
            fail(messageSupplier);
        }
    }

    static boolean isResolvable(Instance<?> instance) {
        return !isUnresolvable(instance);
    }

    static boolean isUnresolvable(Instance<?> instance) {
        return instance.isUnsatisfied() || instance.isAmbiguous();
    }

}
