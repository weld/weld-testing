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

import jakarta.enterprise.inject.spi.Extension;

/**
 * Adds the listed classes as portable CDI extensions to the deployed testing container.
 *
 * @author Björn Kautler
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Repeatable(AddExtensions.All.class)
public @interface AddExtensions {
    Class<? extends Extension>[] value();

    /**
     * Container annotation for repeatable {@link AddExtensions}.
     *
     * @author Björn Kautler
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    @Inherited
    @interface All {
        AddExtensions[] value();
    }
}
