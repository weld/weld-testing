package org.jboss.weld.junit5.autoconfig.beans;


import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class V6 implements Engine, Serializable {

  private int throttle = 0;

  public int getThrottle() {
    return throttle;
  }

  @Override
  public void setThrottle(int throttle) {
    this.throttle = throttle;
  }

}
