package org.jboss.weld.junit5.auto;


import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AddBeanClasses.All.class)
public @interface AddBeanClasses {

  Class<?>[] value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface All {
    AddBeanClasses[] value();
  }

}
