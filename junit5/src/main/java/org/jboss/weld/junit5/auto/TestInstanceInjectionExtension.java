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

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Singleton;

import org.jboss.weld.injection.ForwardingInjectionTarget;

/**
 * Extension that makes a test instance appear like a regular bean even though instantiated by JUnit.
 * Injection into all test instances, also {@link org.junit.jupiter.api.Nested &#064;Nested} ones, is handled in {@link org.jboss.weld.junit5.WeldInitiator#addObjectsToInjectInto} and related.
 * Proper handling of all other CDI annotations such as {@link jakarta.enterprise.inject.Produces &#064;Produces} is supported only on top level test classes.
 */
public class TestInstanceInjectionExtension<T> implements Extension {

    private static final AnnotationLiteral<Singleton> SINGLETON_LITERAL = new AnnotationLiteral<Singleton>() {};

    private Class<?> testClass;
    private T testInstance;

    TestInstanceInjectionExtension(T testInstance) {
        this.testClass = testInstance.getClass();
        this.testInstance = testInstance;
    }

    void rewriteTestClassScope(@Observes ProcessAnnotatedType<T> pat, BeanManager beanManager) {

        if (pat.getAnnotatedType().getJavaClass().equals(testClass)) {
            pat.configureAnnotatedType().add(SINGLETON_LITERAL);
        }

    }

    private class TestInstanceInjectionTarget extends ForwardingInjectionTarget<T> {

        private InjectionTarget<T> injectionTarget;

        TestInstanceInjectionTarget(InjectionTarget<T> injectionTarget) {
            this.injectionTarget = injectionTarget;
        }

        @Override
        protected InjectionTarget<T> delegate() {
            return injectionTarget;
        }

        @Override
        public T produce(CreationalContext<T> creationalContext) {
            return testInstance;
        }

    }

    void rewriteTestInstanceInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {

        if (pit.getAnnotatedType().getJavaClass().equals(testClass)) {
            pit.setInjectionTarget(new TestInstanceInjectionTarget(pit.getInjectionTarget()));
        }

    }

}
