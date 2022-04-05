/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.spock.auto;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Produces;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@code @ExcludeBean} excludes a bean, or multiple beans, that include a bean defining annotation
 * (e.g. scope) from automatic discovery. This can be helpful to allow replacing a bean class with a different
 * implementation, typically a mock or stub.
 *
 * <p>The type of bean to exclude is implied by the annotated fields' type or annotated methods' return type. If the type
 * is a base class or interface all beans extending / implementing that type will be excluded.
 *
 * <p>NOTE: This annotation will only exclude beans defined by class annotations. It will not exclude beans of the
 * implied type that are defined by {@link Produces} producer methods / fields or synthetic
 * beans. Also, current implementation excludes beans based on type, disregarding any qualifiers that are specified.
 *
 * <p><b>Example:</b>
 * <pre>
 * &#64;EnableWeld(automagic = true)
 * class TestSomeFoo extends Specification {
 *   &#64;Inject
 *   SomeFoo someFoo        // SomeFoo depends upon application scoped bean Foo
 *
 *   &#64;Produces
 *   &#64;ExcludeBean           // Excludes beans with type Foo from automatic discovery
 *   Foo mockFoo = Mock()   // mockFoo is now produced in place of original Foo impl
 *
 *   def test(Foo myFoo) {
 *     expect:
 *       myFoo?.bar == 'mock-foo'
 *   }
 * }
 * </pre>
 *
 * @author Björn Kautler
 * @see ExcludeBeanClasses
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@Inherited
public @interface ExcludeBean {
}
