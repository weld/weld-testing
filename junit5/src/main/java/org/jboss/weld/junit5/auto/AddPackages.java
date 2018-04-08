package org.jboss.weld.junit5.auto;


import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddPackages.All.class)
public @interface AddPackages {

  Class<?>[] value();

  boolean recursively() default true;

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface All {
    AddPackages[] value();
  }

}
