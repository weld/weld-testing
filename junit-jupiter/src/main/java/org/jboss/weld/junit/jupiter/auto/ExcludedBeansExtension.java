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
package org.jboss.weld.junit.jupiter.auto;

import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.inject.Scope;

/**
 * Extension class that ensures selected classes are excluded as
 * beans.
 */
public class ExcludedBeansExtension implements Extension {

    private final Set<Type> excludedBeanTypes;
    private final Set<Class<?>> excludedBeanClasses;

    ExcludedBeansExtension(Set<Type> excludedBeanTypes, Set<Class<?>> excludedBeanClasses) {
        this.excludedBeanTypes = excludedBeanTypes;
        this.excludedBeanClasses = excludedBeanClasses;
    }

    <T> void excludeBeans(@Observes @WithAnnotations({ Scope.class, NormalScope.class }) ProcessAnnotatedType<T> pat) {

        if (excludedBeanClasses.contains(pat.getAnnotatedType().getJavaClass())) {
            pat.veto();
            return;
        }

        Set<Type> typeClosure = pat.getAnnotatedType().getTypeClosure();
        for (Type excludedBeanType : excludedBeanTypes) {
            if (typeClosure.contains(excludedBeanType)) {
                pat.veto();
                return;
            }
        }
    }

}
