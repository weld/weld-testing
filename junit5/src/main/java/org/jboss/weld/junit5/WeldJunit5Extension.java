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

import static org.jboss.weld.junit5.ExtensionContextUtils.getContainerFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.getEnrichersFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.getExplicitInjectionInfoFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.getInitiatorFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.removeContainerFromStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.setContainerToStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.setEnrichersToStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.setExplicitInjectionInfoToStore;
import static org.jboss.weld.junit5.ExtensionContextUtils.setInitiatorToStore;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.util.collections.ImmutableList;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * JUnit 5 extension allowing to bootstrap Weld SE container for each @Test method (or once per test class
 * if running {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_CLASS}) and tear it down afterwards. Also allows
 * injecting CDI beans as parameters to @Test methods and resolves all @Inject fields in test class.
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
        boolean globalSettings = Boolean.parseBoolean(System.getProperty(GLOBAL_EXPLICIT_PARAM_INJECTION, "false"));
        if (globalSettings) {
            setExplicitInjectionInfoToStore(ec, true);
            return;
        }
        // check class-level annotation
        Class<?> inspectedTestClass = ec.getRequiredTestClass();
        ExplicitParamInjection explicitParamInjection = inspectedTestClass.getAnnotation(ExplicitParamInjection.class);
        if (explicitParamInjection != null) {
            setExplicitInjectionInfoToStore(ec, explicitParamInjection.value());
        } else {
            // if not found, it can still be a nested class
            // inspect enclosing classes until first annotation is found or until we hit top-level class
            inspectedTestClass = inspectedTestClass.getEnclosingClass();
            while (inspectedTestClass != null && explicitParamInjection == null) {
                explicitParamInjection = inspectedTestClass.getAnnotation(ExplicitParamInjection.class);
                if (explicitParamInjection != null) {
                    setExplicitInjectionInfoToStore(ec, explicitParamInjection.value());
                }
                inspectedTestClass = inspectedTestClass.getEnclosingClass();
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
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
    public void beforeEach(ExtensionContext extensionContext) {
        startWeldContainerIfAppropriate(PER_METHOD, extensionContext);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (determineTestLifecycle(context).equals(PER_METHOD)) {
            WeldInitiator initiator = getInitiatorFromStore(context);
            if (initiator != null) {
                // Clean up all auto-closeable sources in the Store
                removeContainerFromStore(context);
                // Perform Weld container shut down
                initiator.shutdownWeld();
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (determineTestLifecycle(context).equals(PER_CLASS)) {
            WeldInitiator initiator = getInitiatorFromStore(context);
            if (initiator != null) {
                // Clean up all auto-closeable sources in the Store
                removeContainerFromStore(context);
                // Perform Weld container shut down
                initiator.shutdownWeld();
            }
        }
    }

    protected void weldInit(ExtensionContext context, Weld weld, WeldInitiator.Builder weldInitiatorBuilder) {
        weld.addPackage(false, context.getRequiredTestClass());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        // we did our checks in supportsParameter() method, now we can do simple resolution
        if (getContainerFromStore(extensionContext) != null) {
            List<Annotation> qualifiers = resolveQualifiers(parameterContext,
                    getContainerFromStore(extensionContext).getBeanManager());
            return getContainerFromStore(extensionContext)
                    .select(parameterContext.getParameter().getParameterizedType(),
                            qualifiers.toArray(new Annotation[qualifiers.size()]))
                    .get();
        }
        return null;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        // do not attempt to resolve JUnit 5 built-in parameters
        if (isJUnitResolvedParameter(parameterContext)) {
            return false;
        }
        // if weld container isn't up yet or if it's not Method, we don't resolve it
        if (getContainerFromStore(extensionContext) == null
                || (!(parameterContext.getDeclaringExecutable() instanceof Method))) {
            return false;
        }
        List<Annotation> qualifiers = resolveQualifiers(parameterContext,
                getContainerFromStore(extensionContext).getBeanManager());
        // if we require explicit parameter injection (via global settings or annotation) and there are no qualifiers we don't resolve it
        // if the method is annotated @ParameterizedTest, we treat it as explicit param injection and require qualifiers
        if ((getExplicitInjectionInfoFromStore(extensionContext)
                || methodRequiresExplicitParamInjection(parameterContext)
                || methodIsParameterizedTest(parameterContext))
                && qualifiers.isEmpty()) {
            return false;
        } else {
            // attempt to resolve the bean; at this point we know it should be a CDI bean since it has CDI qualifiers
            // if resolution fails, throw an exception
            WeldInstance<?> select = getContainerFromStore(extensionContext).select(
                    parameterContext.getParameter().getParameterizedType(),
                    qualifiers.toArray(new Annotation[qualifiers.size()]));
            if (!select.isResolvable()) {
                throw new ParameterResolutionException(String.format(
                        "Weld has failed to resolve test parameter [%s] in method [%s].%n" +
                                "%s dependency has type %s and qualifiers %s.",
                        parameterContext.getParameter(), parameterContext.getDeclaringExecutable().toGenericString(),
                        select.isAmbiguous() ? "Ambiguous" : "Unsatisfied",
                        parameterContext.getParameter().getType().getName(), qualifiers));
            }
            return true;
        }
    }

    /**
     * @see {@code org.junit.jupiter.engine.extension.TestInfoParameterResolver.supportsParameter}
     * @see {@code org.junit.jupiter.engine.extension.RepetitionExtension.supportsParameter}
     * @see {@code org.junit.jupiter.engine.extension.TestReporterParameterResolver.supportsParameter}
     * @see {@code org.junit.jupiter.engine.extension.TempDirectory.supportsParameter}
     */
    private boolean isJUnitResolvedParameter(ParameterContext parameterContext) {
        Class<?> type = parameterContext.getParameter().getType();
        if (type == TestInfo.class || type == RepetitionInfo.class || type == TestReporter.class) {
            return true;
        }
        if (parameterContext.isAnnotated(TempDir.class)) {
            return true;
        }
        return false;
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
        ExplicitParamInjection ann = pc.getDeclaringExecutable().getAnnotation(ExplicitParamInjection.class);
        if (ann != null) {
            return ann.value();
        }
        return false;
    }

    private boolean methodIsParameterizedTest(ParameterContext pc) {
        return pc.getDeclaringExecutable().getAnnotation(ParameterizedTest.class) != null ? true : false;
    }

    private TestInstance.Lifecycle determineTestLifecycle(ExtensionContext ec) {
        Optional<TestInstance.Lifecycle> testInstanceLifecycle = ec.getTestInstanceLifecycle();
        if (testInstanceLifecycle.isEmpty()) {
            // this should never happen, but if so, we assume default
            return PER_METHOD;
        } else {
            return testInstanceLifecycle.get();
        }
    }

    private void startWeldContainerIfAppropriate(TestInstance.Lifecycle expectedLifecycle, ExtensionContext context) {
        // if the lifecycle is what we expect it to be, start Weld container
        if (determineTestLifecycle(context).equals(expectedLifecycle)) {
            Object testInstance = context.getRequiredTestInstance();

            // store info about explicit param injection, either from global settings or from annotation on the test class
            storeExplicitParamResolutionInformation(context);

            // iterate through the testInstance, the enclosing instance (in case of nested tests),
            // the enclosing instance of the enclosing instance (in cases of twice nested tests) and so on
            // until we find a WeldInitiator
            final List<Object> allTestInstances = new ArrayList<>(context.getRequiredTestInstances().getAllInstances());
            Collections.reverse(allTestInstances); // so we can iterate from inner-most to outer-most
            WeldInitiator initiator = allTestInstances.stream()
                    .map(this::findInitiatorInInstance)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> getDefaultInitiator(context, testInstance));
            setInitiatorToStore(context, initiator);

            // this ensures the test class is injected into
            // in case of nested tests, this also injects into any outer classes
            initiator.addObjectsToInjectInto(new HashSet<>(allTestInstances));

            // and finally, init Weld
            setContainerToStore(context, initiator.initWeld(testInstance));
        }
    }

    private WeldInitiator findInitiatorInInstance(Object testInstance) {
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
                        try {
                            fieldInstance = field.get(testInstance);
                        } catch (IllegalAccessException e2) {
                            // we should never get to this point, because setAccessible would have thrown earlier if access could
                            // not be granted.
                            throw new AssertionError();
                        }
                    }
                    if (fieldInstance instanceof WeldInitiator) {
                        initiator = (WeldInitiator) fieldInstance;
                        foundInitiatorFields.add(field);
                    } else {
                        // Field with other value than WeldInitiator was annotated with @WeldSetup
                        throw new IllegalStateException("@WeldSetup annotation should only be used on a field with a "
                                + "WeldInitiator value but was found on field " + field.getName() + "with a "
                                + ((fieldInstance == null) ? "null" : fieldInstance.getClass())
                                + " value which is declared in class " + field.getDeclaringClass());
                    }
                }
            }
        }
        if (foundInitiatorFields.isEmpty()) {
            return null;
        }
        validateInitiator(foundInitiatorFields);
        // Multiple occurrences of @WeldSetup in the hierarchy will lead to an exception
        if (foundInitiatorFields.size() > 1) {
            final String msg = foundInitiatorFields.stream()
                    .map(f -> "Field '" + f.getName() + "' with type " + f.getType() + " which is declared in "
                            + f.getDeclaringClass())
                    .collect(Collectors.joining("\n", "Multiple @WeldSetup annotated fields found, "
                            + "only one is allowed! Fields found:\n", ""));
            throw new IllegalStateException(msg);
        }

        return initiator;
    }

    private WeldInitiator getDefaultInitiator(ExtensionContext context, Object testInstance) {

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

        return builder.build();
    }

    protected void validateInitiator(List<Field> foundInitiatorFields) {
        // a found initiator is always good for this variant
    }
}
