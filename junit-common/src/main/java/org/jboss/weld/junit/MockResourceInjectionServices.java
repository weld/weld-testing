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
package org.jboss.weld.junit;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Resource;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.injection.spi.helpers.SimpleResourceReference;
import org.jboss.weld.junit.AbstractWeldInitiator.AbstractBuilder;
import org.jboss.weld.util.collections.ImmutableMap;

/**
 *
 * @author Martin Kouba
 * @see AbstractBuilder#bindResource(String, Object)
 */
public class MockResourceInjectionServices implements ResourceInjectionServices {

    private static final String RESOURCE_LOOKUP_PREFIX = "java:comp/env";

    private final Map<String, Object> resources;

    public MockResourceInjectionServices(Map<String, Object> resources) {
        this.resources = ImmutableMap.copyOf(resources);
    }

    // only for Weld 2
    public Object resolveResource(InjectionPoint injectionPoint) {
        Resource resource = getResourceAnnotation(injectionPoint);
        if (resource == null) {
            throw new IllegalArgumentException("No @Resource annotation found on " + injectionPoint);
        }
        if (injectionPoint.getMember() instanceof Method && ((Method) injectionPoint.getMember()).getParameterTypes().length != 1) {
            throw new IllegalArgumentException(
                    "Injection point represents a method which doesn't follow JavaBean conventions (must have exactly one parameter) " + injectionPoint);
        }
        String name;
        if (!resource.lookup().equals("")) {
            name = resource.lookup();
        } else {
            name = getResourceName(injectionPoint);
        }
        return resources.get(name);
    }

    @Override
    public ResourceReferenceFactory<Object> registerResourceInjectionPoint(InjectionPoint injectionPoint) {
        return new ResourceReferenceFactory<Object>() {
            @Override
            public ResourceReference<Object> createResource() {
                return new SimpleResourceReference<Object>(resolveResource(injectionPoint));
            }
        };
    }

    @Override
    public ResourceReferenceFactory<Object> registerResourceInjectionPoint(String jndiName, String mappedName) {
        throw new UnsupportedOperationException();
    }

    // only for Weld 2
    public Object resolveResource(String jndiName, String mappedName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() {
    }

    private String getResourceName(InjectionPoint injectionPoint) {
        Resource resource = getResourceAnnotation(injectionPoint);
        String mappedName = resource.mappedName();
        if (!mappedName.equals("")) {
            return mappedName;
        }
        String name = resource.name();
        if (!name.equals("")) {
            return RESOURCE_LOOKUP_PREFIX + "/" + name;
        }
        String propertyName;
        if (injectionPoint.getMember() instanceof Field) {
            propertyName = injectionPoint.getMember().getName();
        } else if (injectionPoint.getMember() instanceof Method) {
            propertyName = getPropertyName((Method) injectionPoint.getMember());
            if (propertyName == null) {
                throw new IllegalArgumentException("Injection point represents a method which doesn't follow "
                        + "JavaBean conventions (unable to determine property name) " + injectionPoint);
            }
        } else {
            throw new AssertionError("Unable to inject into " + injectionPoint);
        }
        String className = injectionPoint.getMember().getDeclaringClass().getName();
        return RESOURCE_LOOKUP_PREFIX + "/" + className + "/" + propertyName;
    }

    private Resource getResourceAnnotation(InjectionPoint injectionPoint) {
        Annotated annotated = injectionPoint.getAnnotated();
        if (annotated instanceof AnnotatedParameter<?>) {
            annotated = ((AnnotatedParameter<?>) annotated).getDeclaringCallable();
        }
        return annotated.getAnnotation(Resource.class);
    }

    private static String getPropertyName(Method method) {
        String methodName = method.getName();
        if (methodName.matches("^(get).*") && method.getParameterTypes().length == 0) {
            return Introspector.decapitalize(methodName.substring(3));
        } else if (methodName.matches("^(is).*") && method.getParameterTypes().length == 0) {
            return Introspector.decapitalize(methodName.substring(2));
        } else {
            return null;
        }

    }

}
