package org.jboss.weld.junit5.auto.beans.unsatisfied;

import java.io.Serializable;

import org.jboss.weld.junit5.auto.beans.Engine;

// NOTE - deliberately missing bean defining annotation
public class V8NoAnnotation implements Engine, Serializable {

    private int throttle = 0;

    public int getThrottle() {
        return throttle;
    }

    @Override
    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }

}
