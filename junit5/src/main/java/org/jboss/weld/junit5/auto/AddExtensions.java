package org.jboss.weld.junit5.auto;


import java.lang.annotation.*;

import javax.enterprise.inject.spi.Extension;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddExtensions.All.class)
public @interface AddExtensions {

  Class<? extends Extension>[] value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface All {
    AddExtensions[] value();
  }

}
