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

package org.jboss.weld.spock;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import spock.lang.Shared;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation with which Weld can be disabled on specification or feature level if it was
 * enabled globally or on specification level.
 *
 * <p>If Weld is enabled for a specification globally or by annotation with specification scope,
 * and disabled for a feature, there will still be the specification scoped Weld running during the
 * test and {@link Shared @Shared} fields injected from that instance, but the non-{@code @Shared}
 * fields, and parameters of {@code setup}, feature, and {@code cleanup} methods will not be injected.
 *
 * <p>On any class that is not a specification and on any method that is not a feature, this annotation
 * is simply ignored and has no effect.
 *
 * <p>If this annotation is applied on the same element as {@link EnableWeld @EnableWeld}, an exception
 * is thrown as it is unclear which should have precedence.
 *
 * @author Björn Kautler
 * @see EnableWeld
 * @see org.jboss.weld.spock.impl.EnableWeldExtension
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Documented
public @interface DisableWeld {
}
