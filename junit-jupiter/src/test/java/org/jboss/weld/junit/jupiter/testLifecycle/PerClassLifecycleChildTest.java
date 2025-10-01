package org.jboss.weld.junit.jupiter.testLifecycle;

// all configuration is inherited, so are test methods
// we should recognize this class as having per-class lifecycle and bootstrap Weld accordingly
public class PerClassLifecycleChildTest extends PerClassLifecycleTest {

    static String childContainerId = null;

    @Override
    String getContainerId() {
        return childContainerId;
    }

    @Override
    void setContainerId(String id) {
        childContainerId = id;
    }
}
