package org.jboss.weld.junit5;

import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddInterceptors.All.class)
public @interface AddInterceptors {

  Class<?>[] value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface All {
    AddInterceptors[] value();
  }

}
