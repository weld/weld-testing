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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Qualifier;

import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.junit.AbstractWeldInitiator;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * JUnit 5 extension allowing to bootstrap Weld SE container for each @Test method and tear it down afterwards. Also allows to
 * inject CDI beans as parameters to @Test methods and resolves all @Inject fields in test class.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class WeldJunit5Extension implements AfterAllCallback, TestInstancePostProcessor, AfterTestExecutionCallback, ParameterResolver {

    private static final String INITIATOR = "weldInitiator";
    private static final String CONTAINER = "weldContainer";

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (determineTestLifecycle(context).equals(TestInstance.Lifecycle.PER_CLASS)) {
            getInitiatorFromStore(context).shutdownWeld();
        }
        // clear all that we put into Store
        clearStore(context);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        if (determineTestLifecycle(context).equals(TestInstance.Lifecycle.PER_METHOD)) {
            getInitiatorFromStore(context).shutdownWeld();
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

        // obtain WeldInitiator if defined, we have to use reflections here
        // first check if we don't alredy have WeldInitiator (in per-method lifecycle this happends where there are multiple tests)s
        if (getInitiatorFromStore(context) == null) {
            for (Field field : testInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(WeldSetup.class)) {
                    if (getInitiatorFromStore(context) != null) {
                        // multiple fields found, throw exception
                        throw new IllegalStateException("Multiple @WeldSetup annotated fields found, please use only one such field.");
                    }
                    Object fieldInstance;
                    try {
                        fieldInstance = field.get(testInstance);
                    } catch (IllegalAccessException e) {
                        // In case we cannot get to the field, we need to set accessibility as well
                        field.setAccessible(true);
                        fieldInstance = field.get(testInstance);
                    }
                    if (fieldInstance instanceof WeldInitiator) {
                        getStore(context).put(INITIATOR, (WeldInitiator) fieldInstance);
                    } else {
                        // Field with other type than WeldInitiator was annotated with @WeldSetup
                        throw new IllegalStateException("@WeldSetup annotation should only be used on a field of type WeldInitiator.");
                    }
                }
            }
        }

        // WeldInitiator may still be null if user didn't specify it at all, we need to create it
        if (getInitiatorFromStore(context) == null) {
            getStore(context).put(INITIATOR, WeldInitiator.from(AbstractWeldInitiator.createWeld().addPackage(false, testInstance.getClass())).build());
        }

        // and finally, init Weld
        getStore(context).put(CONTAINER, getInitiatorFromStore(context).initWeld(testInstance));
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        // see if container is up, if not, we do not support it
        if (getContainerFromStore(extensionContext) != null) {
            List<Annotation> qualifiers = resolveQualifiers(parameterContext);
            return getContainerFromStore(extensionContext).select(parameterContext.getParameter().getType(), qualifiers.toArray(new Annotation[qualifiers.size()])).get();
        }
        return null;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        // see if container is up, if not, we do not support it
        if (getContainerFromStore(extensionContext) != null) {
            List<Annotation> qualifiers = resolveQualifiers(parameterContext);
            if (getContainerFromStore(extensionContext).select(parameterContext.getParameter().getType(), qualifiers.toArray(new Annotation[qualifiers.size()])).isResolvable()) {
                return true;
            }
        }
        return false;
    }

    private List<Annotation> resolveQualifiers(ParameterContext pc) {
        List<Annotation> qualifiers = new ArrayList<>();
        if (pc.getParameter().getAnnotations().length == 0) {
            return Collections.emptyList();
        } else {
            for (Annotation annotation : pc.getParameter().getAnnotations()) {
                // check if that annotation is in fact Qualifier
                if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                    qualifiers.add(annotation);
                }
            }
        }
        return qualifiers;
    }

    private TestInstance.Lifecycle determineTestLifecycle(ExtensionContext ec) {
        // check the test for import org.junit.jupiter.api.TestInstance annotation
        TestInstance annotation = ec.getRequiredTestClass().getAnnotation(TestInstance.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            return TestInstance.Lifecycle.PER_METHOD;
        }
    }

    /**
     * We use custom namespace based on this extension class and test class
     */
    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestClass()));
    }

    /**
     * Can return null if WeldInitiator isn't stored yet
     */
    private WeldInitiator getInitiatorFromStore(ExtensionContext context) {
        return getStore(context).get(INITIATOR, WeldInitiator.class);
    }

    /**
     * Can return null if WeldContainer isn't stored yet
     */
    private WeldContainer getContainerFromStore(ExtensionContext context) {
        return getStore(context).get(CONTAINER, WeldContainer.class);
    }
    
    private void clearStore(ExtensionContext context) {
        getStore(context).remove(INITIATOR, WeldInitiator.class);
        getStore(context).remove(CONTAINER, WeldContainer.class);
    }
}
