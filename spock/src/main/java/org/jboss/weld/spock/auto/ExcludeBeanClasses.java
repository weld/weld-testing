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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Produces;

/**
 * {@code @ExcludeBeanClasses} excludes a set of classes with bean defining annotations (e.g. scopes) from automatic
 * discovery. This can be helpful to allow replacing bean classes with a different implementation, typically a mock
 * or stub.
 *
 * <p>
 * This annotation works as an inverse of {@link AddBeanClasses} hence usually requires actual bean implementation
 * classes as parameters.
 *
 * <p>
 * NOTE: This annotation will only exclude beans defined by class annotations. It will not exclude beans of the
 * specified type that are defined by {@link Produces} producer methods / fields or synthetic beans.
 *
 * <p>
 * <b>Example:</b>
 *
 * <pre>
 * &#64;EnableWeld(automagic = true)
 * &#64;ExcludeBeanClasses(Foo)   // Excludes Foo bean class from automatic discovery
 * class TestSomeFoo extends Specification {
 *   &#64;Inject
 *   SomeFoo someFoo          // SomeFoo depends upon application scoped bean Foo
 *
 *   &#64;Produces
 *   Foo mockFoo = Mock()     // mockFoo is now produced in place of original Foo impl
 *
 *   &#64;Test
 *   def test(Foo myFoo) {
 *     expect:
 *       myFoo?.bar == 'mock-foo'
 *   }
 * }
 * </pre>
 *
 * @author Björn Kautler
 * @see ExcludeBean
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Repeatable(ExcludeBeanClasses.All.class)
public @interface ExcludeBeanClasses {
    Class<?>[] value();

    /**
     * Container annotation for repeatable {@link ExcludeBeanClasses}.
     *
     * @author Björn Kautler
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    @Inherited
    @interface All {
        ExcludeBeanClasses[] value();
    }
}
