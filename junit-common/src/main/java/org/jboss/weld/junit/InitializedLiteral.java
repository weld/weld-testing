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
package org.jboss.weld.junit;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Initialized;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Supports inline instantiation of the {@link Initialized} qualifier.
 *
 * @author Martin Kouba
 */
class InitializedLiteral extends AnnotationLiteral<Initialized> implements Initialized {

    private static final long serialVersionUID = 1L;

    private final Class<? extends Annotation> value;

    public static InitializedLiteral of(Class<? extends Annotation> value) {
        return new InitializedLiteral(value);
    }

    private InitializedLiteral(Class<? extends Annotation> value) {
        this.value = value;
    }

    public Class<? extends Annotation> value() {
        return value;
    }
}
