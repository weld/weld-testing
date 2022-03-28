package org.jboss.weld.junit5.auto.beans;


import jakarta.enterprise.context.Dependent;

import java.io.Serializable;

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
