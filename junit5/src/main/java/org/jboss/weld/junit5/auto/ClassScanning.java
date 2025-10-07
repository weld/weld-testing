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

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.decorator.Decorator;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.interceptor.Interceptor;

import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.Preconditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides <b>automagic</b> bean class discovery for a test class instance.
 */
class ClassScanning {

    static void scanForRequiredBeanClasses(List<Class<?>> testClasses, Weld weld, boolean explicitInjection) {

        List<Class<?>> classesToProcess = new ArrayList<>();
        classesToProcess.addAll(testClasses);

        Set<Class<?>> foundClasses = new HashSet<>();
        Set<Type> excludedBeanTypes = new HashSet<>();
        Set<Class<?>> excludedBeanClasses = new HashSet<>();
        boolean syntheticArchiveDiscoverySet = false;

        while (!classesToProcess.isEmpty()) {

            Class<?> currClass = classesToProcess.remove(0);

            if (foundClasses.contains(currClass) ||
                    excludedBeanTypes.contains(currClass) || excludedBeanClasses.contains(currClass) ||
                    currClass.isPrimitive() || currClass.isSynthetic() ||
                    currClass.getName().startsWith("java") || currClass.getName().startsWith("sun")) {
                continue;
            }

            foundClasses.add(currClass);

            AnnotationSupport.findAnnotatedFields(currClass, ExcludeBean.class).stream()
                    .map(Field::getType)
                    .forEach(excludedBeanTypes::add);

            AnnotationSupport.findAnnotatedMethods(currClass, ExcludeBean.class, HierarchyTraversalMode.BOTTOM_UP).stream()
                    .map(Method::getReturnType)
                    .forEach(excludedBeanTypes::add);

            AnnotationSupport.findAnnotatedFields(currClass, Inject.class).stream()
                    .map(ClassScanning::unwrapInstanceTypeParameter)
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            AnnotationSupport.findAnnotatedMethods(currClass, Inject.class, HierarchyTraversalMode.BOTTOM_UP).stream()
                    .flatMap(method -> getExecutableParameterTypes(method, explicitInjection).stream())
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            findFirstAnnotatedConstructor(currClass, Inject.class)
                    .map(Stream::of).orElseGet(Stream::empty)
                    .flatMap(cons -> getExecutableParameterTypes(cons, explicitInjection).stream())
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            findAnnotatedDeclaredFields(currClass, Produces.class).stream()
                    .map(Field::getType)
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            findAnnotatedDeclaredMethods(currClass, Produces.class).stream()
                    .flatMap(method -> Stream.concat(
                            getExecutableParameterTypes(method, explicitInjection).stream(),
                            Stream.of(method.getReturnType())))
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            AnnotationSupport.findAnnotatedMethods(currClass, Test.class, HierarchyTraversalMode.BOTTOM_UP).stream()
                    .flatMap(method -> getExecutableParameterTypes(method, explicitInjection).stream())
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            AnnotationSupport.findAnnotatedMethods(currClass, RepeatedTest.class, HierarchyTraversalMode.BOTTOM_UP).stream()
                    .flatMap(method -> getExecutableParameterTypes(method, explicitInjection).stream())
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            AnnotationSupport.findAnnotatedMethods(currClass, BeforeAll.class, HierarchyTraversalMode.BOTTOM_UP).stream()
                    .flatMap(method -> getExecutableParameterTypes(method, explicitInjection).stream())
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            AnnotationSupport.findAnnotatedMethods(currClass, BeforeEach.class, HierarchyTraversalMode.BOTTOM_UP).stream()
                    .flatMap(method -> getExecutableParameterTypes(method, explicitInjection).stream())
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            AnnotationSupport.findAnnotatedMethods(currClass, AfterEach.class, HierarchyTraversalMode.BOTTOM_UP).stream()
                    .flatMap(method -> getExecutableParameterTypes(method, explicitInjection).stream())
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            AnnotationSupport.findAnnotatedMethods(currClass, AfterAll.class, HierarchyTraversalMode.BOTTOM_UP).stream()
                    .flatMap(method -> getExecutableParameterTypes(method, explicitInjection).stream())
                    .forEach(cls -> addClassesToProcess(classesToProcess, cls));

            AnnotationSupport.findRepeatableAnnotations(currClass, AddPackages.class)
                    .forEach(ann -> stream(ann.value())
                            .distinct()
                            .forEach(cls -> weld.addPackage(ann.recursively(), cls)));

            AnnotationSupport.findRepeatableAnnotations(currClass, AddBeanClasses.class).stream()
                    .flatMap(ann -> stream(ann.value()))
                    .distinct()
                    .forEach(it -> {
                        classesToProcess.add(it);
                        weld.addBeanClass(it);
                    });

            AnnotationSupport.findRepeatableAnnotations(currClass, AddExtensions.class).stream()
                    .flatMap(ann -> stream(ann.value()))
                    .distinct()
                    .map(ClassScanning::createExtension)
                    .forEach(weld::addExtension);

            AnnotationSupport.findRepeatableAnnotations(currClass, AddEnabledInterceptors.class).stream()
                    .flatMap(ann -> stream(ann.value()))
                    .distinct()
                    .forEach(interceptor -> {
                        classesToProcess.add(interceptor);
                        weld.addInterceptor(interceptor);
                        weld.addBeanClass(interceptor);
                    });

            AnnotationSupport.findRepeatableAnnotations(currClass, AddEnabledDecorators.class).stream()
                    .flatMap(ann -> stream(ann.value()))
                    .distinct()
                    .forEach(decorator -> {
                        classesToProcess.add(decorator);
                        weld.addDecorator(decorator);
                        weld.addBeanClass(decorator);
                    });

            AnnotationSupport.findRepeatableAnnotations(currClass, EnableAlternatives.class).stream()
                    .flatMap(ann -> stream(ann.value()))
                    .distinct()
                    .forEach(weld::addAlternative);

            AnnotationSupport.findRepeatableAnnotations(currClass, EnableAlternativeStereotypes.class).stream()
                    .flatMap(ann -> stream(ann.value()))
                    .distinct()
                    .forEach(weld::addAlternativeStereotype);

            AnnotationSupport.findRepeatableAnnotations(currClass, ExcludeBeanClasses.class).stream()
                    .flatMap(ann -> stream(ann.value()))
                    .distinct()
                    .forEach(excludedBeanClasses::add);

            // discovery mode can only be set once; we use the first annotation we find
            if (!syntheticArchiveDiscoverySet) {
                Optional<SetBeanDiscoveryMode> annotation = AnnotationSupport.findAnnotation(currClass,
                        SetBeanDiscoveryMode.class);
                if (annotation.isPresent()) {
                    syntheticArchiveDiscoverySet = true;
                    weld.setBeanDiscoveryMode(annotation.get().value());
                }
            }

        }

        for (Class<?> foundClass : foundClasses) {
            if (hasBeanDefiningAnnotation(foundClass)) {
                weld.addBeanClass(foundClass);
            }
        }

        weld.addExtension(new ExcludedBeansExtension(excludedBeanTypes, excludedBeanClasses));
    }

    private static void addClassesToProcess(Collection<Class<?>> classesToProcess, Type type) {

        if (type instanceof Class) {

            classesToProcess.add((Class<?>) type);
        } else if (type instanceof ParameterizedType) {

            ParameterizedType ptype = (ParameterizedType) type;

            classesToProcess.add((Class<?>) ptype.getRawType());

            for (Type arg : ptype.getActualTypeArguments()) {
                addClassesToProcess(classesToProcess, arg);
            }

        }

    }

    private static List<Class<?>> getExecutableParameterTypes(Executable executable, boolean explicitInjection) {

        List<Class<?>> types = new ArrayList<>();

        if (explicitInjection) {
            Annotation[][] paramAnns = executable.getParameterAnnotations();
            Class<?>[] paramTypes = executable.getParameterTypes();
            for (int c = 0; c < paramTypes.length; ++c) {
                if (stream(paramAnns[c]).anyMatch(ClassScanning::isBeanParameterAnnotation)) {
                    types.add(paramTypes[c]);
                }
            }
        } else {
            types.addAll(asList(executable.getParameterTypes()));
        }

        return types;
    }

    @SuppressFBWarnings(value = "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION")
    private static Extension createExtension(Class<? extends Extension> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isBeanParameterAnnotation(Annotation ann) {
        return isAnnotated(ann.annotationType(), Qualifier.class);
    }

    private static boolean hasBeanDefiningAnnotation(Class<?> clazz) {
        return isAnnotated(clazz, NormalScope.class) || isAnnotated(clazz, Dependent.class) ||
                isAnnotated(clazz, Interceptor.class) || isAnnotated(clazz, Decorator.class) ||
                isAnnotated(clazz, Stereotype.class);
    }

    private static List<Field> findAllFieldsInHierarchy(Class<?> clazz) {
        Preconditions.notNull(clazz, "Class must not be null");
        List<Field> localFields = getDeclaredFields(clazz).stream().filter((field) -> !field.isSynthetic())
                .collect(Collectors.toList());
        List<Field> superclassFields = getSuperclassFields(clazz).stream()
                .filter((field) -> !isMethodShadowedByLocalFields(field, localFields))
                .collect(Collectors.toList());
        List<Field> methods = new ArrayList<>();
        methods.addAll(superclassFields);
        methods.addAll(localFields);

        return methods;
    }

    private static List<Field> getSuperclassFields(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        return superclass != null && superclass != Object.class ? findAllFieldsInHierarchy(superclass)
                : Collections.emptyList();
    }

    private static List<Field> getDeclaredFields(Class<?> clazz) {
        return asList(clazz.getDeclaredFields());
    }

    private static List<Method> getDeclaredMethods(Class<?> clazz) {
        return asList(clazz.getDeclaredMethods());
    }

    private static boolean isMethodShadowedByLocalFields(Field field, List<Field> localFields) {
        return localFields.stream()
                .anyMatch((local) -> isFieldShadowedBy(field, local));
    }

    private static boolean isFieldShadowedBy(Field upper, Field lower) {
        return upper.getType().equals(lower.getType());
    }

    private static List<Field> findAnnotatedDeclaredFields(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return getDeclaredFields(clazz).stream()
                .filter((field) -> isAnnotated(field, annotationType))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Method> findAnnotatedDeclaredMethods(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return getDeclaredMethods(clazz).stream()
                .filter((field) -> isAnnotated(field, annotationType))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Constructor<?>> getDeclaredConstructors(Class<?> clazz) {
        return asList(clazz.getDeclaredConstructors());
    }

    private static Optional<Constructor<?>> findFirstAnnotatedConstructor(Class<?> clazz,
            Class<? extends Annotation> annotationType) {

        Optional<Constructor<?>> found = getDeclaredConstructors(clazz).stream()
                .filter((cons) -> isAnnotated(cons, annotationType))
                .findFirst();

        if (found.isPresent() || clazz.getSuperclass() == null) {
            return found;
        }

        return findFirstAnnotatedConstructor(clazz.getSuperclass(), annotationType);
    }

    private static Class<?> unwrapInstanceTypeParameter(Field field) {
        Class<?> type = field.getType();
        if (type.equals(Instance.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type typeParameter = parameterizedType.getActualTypeArguments()[0];
            if (typeParameter instanceof ParameterizedType) {
                type = (Class<?>) ((ParameterizedType) typeParameter).getRawType();
            } else {
                type = (Class<?>) typeParameter;
            }
        }
        return type;
    }

}
