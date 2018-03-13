package org.jboss.weld.junit5;


import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(ActivateScopes.All.class)
public @interface ActivateScopes {

  Class<? extends Annotation>[] value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface All {
    ActivateScopes[] value();
  }

}
