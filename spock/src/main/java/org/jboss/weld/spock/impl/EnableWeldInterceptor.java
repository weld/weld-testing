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

package org.jboss.weld.spock.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.inject.spi.BeanManager;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.spock.WeldInitiator;
import org.jboss.weld.spock.WeldSpockEnricher;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;

import static java.util.Collections.singleton;
import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.toList;
import static org.spockframework.runtime.model.MethodInfo.MISSING_ARGUMENT;

/**
 * A Spock interceptor, that serves as base for the manual and automatic interceptors and has the common logic.
 * It first calls the custom logic of the subclass, then adds the shared instance as injection target, initializes
 * the Weld container and after invocation proceeded, shuts down the container. It also provides methods to get a
 * test instance injector and a parameter injector as these need to be attached to varying extension points,
 * depending on the annotation configuration.
 *
 * @author Björn Kautler
 */
abstract class EnableWeldInterceptor implements IMethodInterceptor {
    protected final List<WeldSpockEnricher> weldSpockEnrichers;
    private final Map<Object, WeldInitiator> weldInitiators = synchronizedMap(new WeakHashMap<>());
    private final List<FeatureInfo> handledFeatures = new CopyOnWriteArrayList<>();

    public EnableWeldInterceptor(List<WeldSpockEnricher> weldSpockEnrichers) {
        this.weldSpockEnrichers = weldSpockEnrichers;
    }

    protected abstract WeldInitiator weldInit(IMethodInvocation invocation);

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
        WeldInitiator weldInitiator = weldInit(invocation);
        weldInitiator.addObjectToInjectInto(invocation.getSharedInstance());
        weldInitiator.initWeld(invocation.getInstance());
        try {
            Object id = (invocation.getIteration() == null) ? null : invocation.getInstance();
            weldInitiators.put(id, weldInitiator);
            invocation.proceed();
        } finally {
            weldInitiator.shutdownWeld();
        }
    }

    public IMethodInterceptor getTestInstanceInjector() {
        return invocation -> {
            Object testInstance = invocation.getInstance();

            WeldInitiator weldInitiator = weldInitiators.get(null);
            if (weldInitiator == null) {
                weldInitiator = weldInitiators.get(testInstance);
            }

            try (AutoCloseable contextReleaser = weldInitiator.injectNonContextual(testInstance)) {
                invocation.proceed();
            }
        };
    }

    public void handleFeature(FeatureInfo feature) {
        handledFeatures.add(feature);
    }

    public IMethodInterceptor getParameterInjector(boolean explicitParamInjection) {
        return invocation -> {
            // this is necessary so that a specification scoped interceptor
            // does not inject into a feature or iteration scoped fixture method call
            // or that a feature scoped interceptor of a data-driven feature
            // does not inject into another features' fixture method calls
            if ((invocation.getFeature() != null) && !handledFeatures.contains(invocation.getFeature())) {
                invocation.proceed();
                return;
            }

            // only continue if there still are missing arguments
            if (Arrays.stream(invocation.getArguments())
                    .noneMatch(argument -> argument == MISSING_ARGUMENT)) {
                invocation.proceed();
                return;
            }

            Object testInstance = invocation.getInstance();

            // get the sole weld initiator of this interceptor
            // in case it is around a specification or parameterized feature
            WeldInitiator weldInitiator = weldInitiators.get(null);
            // or get the weld initiator for the current iteration
            if (weldInitiator == null) {
                weldInitiator = weldInitiators.get(testInstance);
            }
            // the fixture method interceptors for all features are triggered for each iteration
            // so if there are multiple features not all have a matching initiator of course
            if (weldInitiator == null) {
                invocation.proceed();
                return;
            }

            BeanManager beanManager = weldInitiator.getBeanManager();
            Parameter[] parameters = invocation.getMethod().getReflection().getParameters();
            Object[] arguments = invocation.getArguments();
            for (int i = 0; i < arguments.length; i++) {
                Object argument = arguments[i];
                if (argument != MISSING_ARGUMENT) {
                    continue;
                }

                Parameter parameter = parameters[i];
                List<Annotation> qualifiers = Arrays
                        .stream(parameter.getAnnotations())
                        .filter(annotation -> beanManager.isQualifier(annotation.annotationType()))
                        .collect(toList());

                if (explicitParamInjection) {
                    if (qualifiers.isEmpty()) {
                        continue;
                    }
                    arguments[i] = weldInitiator
                            .select(parameter.getType(), qualifiers.toArray(new Annotation[0]))
                            .get();
                } else {
                    WeldInstance<?> candidates = weldInitiator
                            .select(parameter.getType(), qualifiers.toArray(new Annotation[0]));
                    if (candidates.isResolvable()) {
                        arguments[i] = candidates.get();
                    }
                }
            }

            invocation.proceed();
        };
    }
}
