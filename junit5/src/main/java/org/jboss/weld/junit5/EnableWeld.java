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
package org.jboss.weld.junit5;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Meta-annotation that allows test classes to be extended with {@code @EnableWeld}
 * instead of {@code @ExtendWith(WeldJunit5Extension.class)}.
 *
 * <pre><br>
 * &#64;EnableWeld
 * public class SimpleTest {
 *
 *     // Injected automatically
 *     &#64;Inject
 *     Foo foo;
 *
 *     &#64;Test
 *     public void testFoo() {
 *         // Weld container is started automatically
 *         assertEquals("baz", foo.getBaz());
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:smoyer1@selesy.com">Steve Moyer</a>
 * @see WeldJunit5Extension
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Inherited
@ExtendWith(WeldJunit5Extension.class)
public @interface EnableWeld {

}
