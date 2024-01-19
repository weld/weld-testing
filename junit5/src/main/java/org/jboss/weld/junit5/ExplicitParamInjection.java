/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.enterprise.inject.Default;

/**
 * An annotation used to enforce explicit parameter annotation. When applied and set to {@code true}, Weld will only attempt to
 * resolve method parameters which have qualifiers. In case no qualifier is required for your bean, add the {@link Default}
 * qualifier, see CDI specification for in depth explanation on qualifiers.
 *
 * This annotation can be applied either on a test class, in which case it affects parameter injection in all methods, or on
 * a test method.
 *
 * Nested classes inherit the behavior declared by their enclosing class but can re-declare this annotation along with the
 * {@link #value()} parameter to override the behavior.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ExplicitParamInjection {

    /**
     * If set to {@code true}, Weld will only attempt to resolve parameters which have CDI qualifier annotations.
     *
     * @return {@code true} by default; can be explicitly set to {@code false} to make Weld attempt to resolve all parameters
     */
    boolean value() default true;

}
