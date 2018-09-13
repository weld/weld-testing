/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.junit5.auto;


import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * {@code @OverrideBean} is, strictly speaking, an always enabled alternative stereotype.
 * It is typically used along with {@code @javax.enterprise.inject.Produces}.
 *
 * It "overrides a bean" that <i>may</i> otherwise be included in the container by providing a new bean (via producer) and
 * selecting it.
 *
 * Here is how such a usage can look like:
 * <pre>
 * &#64;EnableAutoWeld
 * &#64;AddPackages(Foo.class) // this brings in the *original* Foo impl you want to override
 * class OverrideFooTest {
 * 
 *   &#64;Produces
 *   &#64;OverrideBean
 *   Foo fakeFoo = new Foo("non-baz");
 * 
 *   &#64;Test
 *   void test(Foo myFoo) {
 *     assertNotNull(myFoo);
 *     assertEquals(myFoo.getBar(), "non-baz");
 *   }
 * }
 * </pre>
 */
@Stereotype
@Alternative
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface OverrideBean {
}
