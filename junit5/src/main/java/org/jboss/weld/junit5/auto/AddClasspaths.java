package org.jboss.weld.junit5.auto;


import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddClasspaths.All.class)
public @interface AddClasspaths {

  Class<?>[] value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface All {
    AddClasspaths[] value();
  }

}
