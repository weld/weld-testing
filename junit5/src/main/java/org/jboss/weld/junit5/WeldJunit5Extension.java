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

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.util.collections.ImmutableList;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import jakarta.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static org.jboss.weld.junit5.ExtensionContextUtils.getContainerFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.getEnrichersFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.getExplicitInjectionInfoFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.getInitiatorFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.setContainerToStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.setEnrichersToStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.setExplicitInjectionInfoToStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.setInitiatorToStore;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

/**
 * JUnit 5 extension allowing to bootstrap Weld SE container for each @Test method (or per once per test class
 * if running {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_CLASS}) and tear it down afterwards. Also allows to
 * inject CDI beans as parameters to @Test methods and resolves all @Inject fields in test class.
 *
 * <p>
 * If no {@link WeldInitiator} field annotated with {@link WeldSetup} is present on a test class, all service providers of
 * {@link WeldJunitEnricher} interface are used to enrich the default test environment.
 * </p>
 *
 * <pre>
 * &#64;ExtendWith(WeldJunit5Extension.class)
 * public class SimpleTest {
 *
 *     // Injected automatically
 *     &#64;Inject
 *     Foo foo;
 *
 *     &#64;Test
 *     public void testFoo() {
 *         // Weld container is started automatically
 *         assertEquals("baz", foo.getBaz());
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 * @see EnableWeld
 * @see WeldJunitEnricher
 */
public class WeldJunit5Extension implements AfterAllCallback, BeforeAllCallback,
        BeforeEachCallback, AfterEachCallback, ParameterResolver {

    // global system property
    public static final String GLOBAL_EXPLICIT_PARAM_INJECTION = "org.jboss.weld.junit5.explicitParamInjection";

    private static void storeExplicitParamResolutionInformation(ExtensionContext ec) {
        // check system property which may have set the global explicit param injection
        Boolean globalSettings = Boolean.valueOf(System.getProperty(GLOBAL_EXPLICIT_PARAM_INJECTION, "false"));
        if (globalSettings) {
            setExplicitInjectionInfoToStore(ec, true);
            return;
        }
        // check class-level annotation
        for (Annotation annotation : ec.getRequiredTestClass().getAnnotations()) {
            if (annotation.annotationType().equals(ExplicitParamInjection.class)) {
                setExplicitInjectionInfoToStore(ec, true);
                break;
            }
        }

    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (determineTestLifecycle(context).equals(PER_CLASS)) {
            getInitiatorFromStore(context).shutdownWeld();
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // we are storing them into root context, hence only needs to be done once per test suite
        if (getEnrichersFromStore(context) == null) {
            ImmutableList.Builder<WeldJunitEnricher> enrichers = ImmutableList.builder();
            ServiceLoader.load(WeldJunitEnricher.class).forEach(enrichers::add);
            setEnrichersToStore(context, enrichers.build());
        }
        // if the lifecycle is per-class, then we want to start container here
        startWeldContainerIfAppropriate(PER_CLASS, context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (determineTestLifecycle(context).equals(PER_METHOD)) {
            getInitiatorFromStore(context).shutdownWeld();
        }
    }

    protected void weldInit(ExtensionContext context, Weld weld, WeldInitiator.Builder weldInitiatorBuilder) {
        weld.addPackage(false, context.getRequiredTestClass());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        // we did our checks in supportsParameter() method, now we can do simple resolution
        if (getContainerFromStore(extensionContext) != null) {
            List<Annotation> qualifiers = resolveQualifiers(parameterContext, getContainerFromStore(extensionContext).getBeanManager());
            return getContainerFromStore(extensionContext)
                    .select(parameterContext.getParameter().getType(), qualifiers.toArray(new Annotation[qualifiers.size()])).get();
        }
        return null;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        // if weld container isn't up yet or if its not Method, we don't resolve it
        if (getContainerFromStore(extensionContext) == null || (!(parameterContext.getDeclaringExecutable() instanceof Method))) {
            return false;
        }
        List<Annotation> qualifiers = resolveQualifiers(parameterContext, getContainerFromStore(extensionContext).getBeanManager());
        // if we require explicit parameter injection (via global settings or annotation) and there are no qualifiers we don't resolve it
        if ((getExplicitInjectionInfoFromStore(extensionContext) || (methodRequiresExplicitParamInjection(parameterContext))) && qualifiers.isEmpty()) {
            return false;
        } else {
            return getContainerFromStore(extensionContext).select(parameterContext.getParameter().getType(), qualifiers.toArray(new Annotation[qualifiers.size()]))
                    .isResolvable();
        }
    }

    private List<Annotation> resolveQualifiers(ParameterContext pc, BeanManager bm) {
        List<Annotation> qualifiers = new ArrayList<>();
        if (pc.getParameter().getAnnotations().length == 0) {
            return Collections.emptyList();
        } else {
            for (Annotation annotation : pc.getParameter().getAnnotations()) {
                // use BeanManager.isQualifier to be able to detect custom qualifiers which don't need to have @Qualifier
                if (bm.isQualifier(annotation.annotationType())) {
                    qualifiers.add(annotation);
                }
            }
        }
        return qualifiers;
    }

    private boolean methodRequiresExplicitParamInjection(ParameterContext pc) {
        for (Annotation annotation : pc.getDeclaringExecutable().getAnnotations()) {
            if (annotation.annotationType().equals(ExplicitParamInjection.class)) {
                return true;
            }
        }
        return false;
    }

    private TestInstance.Lifecycle determineTestLifecycle(ExtensionContext ec) {
        // check the test for org.junit.jupiter.api.TestInstance annotation
        TestInstance annotation = ec.getRequiredTestClass().getAnnotation(TestInstance.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            return TestInstance.Lifecycle.PER_METHOD;
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        startWeldContainerIfAppropriate(PER_METHOD, extensionContext);
    }

    private void startWeldContainerIfAppropriate(TestInstance.Lifecycle expectedLifecycle, ExtensionContext context) throws Exception {
        // is the lifecycle is what we expect it to be, start Weld container
        if (determineTestLifecycle(context).equals(expectedLifecycle)) {
            Object testInstance = context.getTestInstance().orElseGet(null);
            if (testInstance == null) {
                throw new IllegalStateException("ExtensionContext.getTestInstance() returned empty Optional!");
            }

            // store info about explicit param injection, either from global settings or from annotation on the test class
            storeExplicitParamResolutionInformation(context);

            // all found fields which are WeldInitiator and have @WeldSetup annotation
            List<Field> foundInitiatorFields = new ArrayList<>();
            WeldInitiator initiator = null;
            // We will go through class hierarchy in search of @WeldSetup field (even private)
            for (Class<?> clazz = testInstance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                // Find @WeldSetup field using getDeclaredFields() - this allows even for private fields
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(WeldSetup.class)) {
                        Object fieldInstance;
                        try {
                            fieldInstance = field.get(testInstance);
                        } catch (IllegalAccessException e) {
                            // In case we cannot get to the field, we need to set accessibility as well
                            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                                field.setAccessible(true);
                                return null;
                            });
                            fieldInstance = field.get(testInstance);
                        }
                        if (fieldInstance != null && fieldInstance instanceof WeldInitiator) {
                            initiator = (WeldInitiator) fieldInstance;
                            foundInitiatorFields.add(field);
                        } else {
                            // Field with other type than WeldInitiator was annotated with @WeldSetup
                            throw new IllegalStateException("@WeldSetup annotation should only be used on a field of type"
                                    + " WeldInitiator but was found on a field of type " + field.getType() + " which is declared "
                                    + "in class " + field.getDeclaringClass());
                        }
                    }
                }
            }
            // Multiple occurrences of @WeldSetup in the hierarchy will lead to an exception
            if (foundInitiatorFields.size() > 1) {
                throw new IllegalStateException(foundInitiatorFields.stream().map(f -> "Field type - " + f.getType() + " which is "
                        + "in " + f.getDeclaringClass()).collect(Collectors.joining("\n", "Multiple @WeldSetup annotated fields found, "
                        + "only one is allowed! Fields found:\n", "")));
            }

            // at this point we can be sure that either no or exactly one WeldInitiator was found
            if (initiator == null) {
                Weld weld = WeldInitiator.createWeld();
                WeldInitiator.Builder builder = WeldInitiator.from(weld);

                weldInit(context, weld, builder);

                // Apply discovered enrichers
                for (WeldJunitEnricher enricher : getEnrichersFromStore(context)) {
                    String property = System.getProperty(enricher.getClass().getName());
                    if (property == null || Boolean.parseBoolean(property)) {
                        enricher.enrich(testInstance, context, weld, builder);
                    }
                }

                initiator = builder.build();
            }
            setInitiatorToStore(context, initiator);

            // this ensures the test class is injected into
            // in case of nested tests, this also injects into any outer classes
            initiator.addObjectsToInjectInto(context.getRequiredTestInstances().getAllInstances().stream().collect(Collectors.toSet()));

            // and finally, init Weld
            setContainerToStore(context, initiator.initWeld(testInstance));
        }
    }

}
