package org.jboss.weld.junit5.auto;


import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(EnableAlternativeStereotypes.All.class)
public @interface EnableAlternativeStereotypes {

  Class<? extends Annotation>[] value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface All {
    EnableAlternativeStereotypes[] value();
  }

}
