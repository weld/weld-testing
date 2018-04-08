package org.jboss.weld.junit5.auto;

import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddEnabledInterceptors.All.class)
public @interface AddEnabledInterceptors {

  Class<?>[] value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface All {
    AddEnabledInterceptors[] value();
  }

}
