package org.jboss.weld.junit5;


import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(EnableAlternatives.All.class)
public @interface EnableAlternatives {

  Class<?>[] value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface All {
    EnableAlternatives[] value();
  }

}
