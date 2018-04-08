package org.jboss.weld.junit5.auto;


import java.lang.annotation.*;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;



@Stereotype
@Alternative
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
public @interface OverrideBean {
}
