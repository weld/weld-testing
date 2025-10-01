package org.jboss.weld.junit.jupiter.auto.beans;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;

@Dependent
public class V8 implements Engine, Serializable {

    private int throttle = 0;

    public int getThrottle() {
        return throttle;
    }

    @Override
    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }

}
