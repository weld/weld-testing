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

import static org.jboss.weld.spock.EnableWeld.Scope.FEATURE;
import static org.jboss.weld.spock.EnableWeld.Scope.ITERATION;
import static org.jboss.weld.spock.EnableWeld.Scope.SPECIFICATION;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.jboss.weld.spock.DisableWeld;
import org.jboss.weld.spock.EnableWeld;
import org.jboss.weld.spock.EnableWeld.Scope;
import org.jboss.weld.spock.WeldConfiguration;
import org.jboss.weld.spock.WeldSpockEnricher;
import org.jboss.weld.util.collections.ImmutableList;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.Shared;

/**
 * A global Spock extension evaluating the {@link EnableWeld @EnableWeld}, and {@link DisableWeld @DisableWeld} annotations,
 * and the Spock configuration file options.
 *
 * <p>
 * If a feature has an {@code @EnableWeld} or {@code @DisableWeld} annotation applied, this is what is effective.
 * If Weld is disabled, no container will be started for the feature or the iterations and no non-{@link Shared @Shared}
 * fields, or method parameters will be injected. However, if a for the specification a {@code SPECIFICATION} scoped
 * Weld was booted due to annotation or configuration file options, this container will have injected instances into
 * the {@code @Shared} fields already.
 *
 * <p>
 * If a feature has neither of the two annotations applied, the configuration of the specification is inherited.
 *
 * <p>
 * If a specification has an {@code @EnableWeld} or {@code @DisableWeld} annotation applied, this is effective for
 * all features that do not have an own annotation. It will have the same effect as if the annotation is copied to all
 * features that have none of the two annotations already, except if scope {@code SPECIFICATION} is selected, as this
 * is only valid in a specification level annotation or the Spock configuration file.
 *
 * <p>
 * If a specification has neither of the two annotations applied, the super specifications are searched in order
 * and if an annotated one is found, its annotation is effective as if it were on the specification directly.
 *
 * <p>
 * If also no super specification has any of the annotations, the settings from the Spock configuration file
 * or the respective default settings are effective.
 *
 * @author Björn Kautler
 * @see EnableWeld
 * @see DisableWeld
 * @see WeldConfiguration
 */
public class EnableWeldExtension implements IGlobalExtension {
    private final WeldConfiguration weldConfiguration;
    private volatile List<WeldSpockEnricher> weldSpockEnrichers;

    public EnableWeldExtension(WeldConfiguration weldConfiguration) {
        this.weldConfiguration = weldConfiguration;
    }

    @Override
    public void start() {
        ImmutableList.Builder<WeldSpockEnricher> enrichers = ImmutableList.builder();
        ServiceLoader.load(WeldSpockEnricher.class).forEach(enrichers::add);
        weldSpockEnrichers = enrichers.build();
    }

    @Override
    public void visitSpec(SpecInfo spec) {
        Optional<SpecInfo> optionalAnnotatedSpec = spec
                .getSpecsBottomToTop()
                .stream()
                .filter(specInfo -> specInfo.isAnnotationPresent(EnableWeld.class)
                        || specInfo.isAnnotationPresent(DisableWeld.class))
                .findFirst();

        boolean doEnableWeldForSpec;
        boolean specAutomagic;
        Scope specScope;
        boolean specExplicitParamInjection;
        if (optionalAnnotatedSpec.isPresent()) {
            SpecInfo annotatedSpec = optionalAnnotatedSpec.get();
            EnableWeld enableWeld = annotatedSpec.getAnnotation(EnableWeld.class);
            boolean enableWeldForSpec = enableWeld != null;
            boolean disableWeldForSpec = annotatedSpec.isAnnotationPresent(DisableWeld.class);

            if (enableWeldForSpec && disableWeldForSpec) {
                throw new InvalidSpecException(
                        "@EnableWeld and @DisableWeld must not be used on the same spec: " + annotatedSpec.getDisplayName());
            }

            if (enableWeldForSpec) {
                doEnableWeldForSpec = true;
                specAutomagic = enableWeld.automagic();
                specScope = enableWeld.scope();
                specExplicitParamInjection = enableWeld.explicitParamInjection();
            } else {
                doEnableWeldForSpec = false;
                specAutomagic = false;
                specScope = null;
                specExplicitParamInjection = false;
            }
        } else {
            doEnableWeldForSpec = weldConfiguration.enabled;
            specAutomagic = weldConfiguration.automagic;
            specScope = weldConfiguration.scope == null ? ITERATION : weldConfiguration.scope;
            specExplicitParamInjection = weldConfiguration.explicitParamInjection;
        }

        // boot Weld around specification and inject shared fields
        EnableWeldInterceptor enableWeldInterceptorForSpec;
        if (doEnableWeldForSpec && (specScope == SPECIFICATION)) {
            enableWeldInterceptorForSpec = specAutomagic
                    ? new EnableWeldAutoInterceptor(weldSpockEnrichers, specExplicitParamInjection)
                    : new EnableWeldManualInterceptor(weldSpockEnrichers);
            spec.addInterceptor(enableWeldInterceptorForSpec);

            // inject parameters for specification fixture methods
            Stream
                    .concat(
                            spec.getSetupSpecMethods().stream(),
                            spec.getCleanupSpecMethods().stream())
                    .forEach(method -> attachParameterInjector(method, enableWeldInterceptorForSpec,
                            specExplicitParamInjection));
        } else {
            enableWeldInterceptorForSpec = null;
        }

        spec
                .getAllFeatures()
                .forEach(feature -> visitFeature(feature, doEnableWeldForSpec, specAutomagic, specScope,
                        specExplicitParamInjection, enableWeldInterceptorForSpec));
    }

    private void visitFeature(FeatureInfo feature, boolean doEnableWeldForSpec, boolean specAutomagic, Scope specScope,
            boolean specExplicitParamInjection, EnableWeldInterceptor enableWeldInterceptorForSpec) {
        MethodInfo featureMethod = feature.getFeatureMethod();
        EnableWeld enableWeld = featureMethod.getAnnotation(EnableWeld.class);
        boolean enableWeldForFeature = enableWeld != null;
        boolean disableWeldForFeature = featureMethod.isAnnotationPresent(DisableWeld.class);

        if (enableWeldForFeature && disableWeldForFeature) {
            throw new InvalidSpecException(
                    "@EnableWeld and @DisableWeld must not be used on the same feature: " + feature.getDisplayName());
        }

        boolean doEnableWeldForFeature;
        boolean featureAutomagic;
        Scope featureScope;
        boolean featureExplicitParamInjection;
        if (enableWeldForFeature) {
            doEnableWeldForFeature = true;
            featureAutomagic = enableWeld.automagic();
            featureScope = enableWeld.scope();
            if (featureScope.compareTo(FEATURE) > 0) {
                throw new InvalidSpecException("@EnableWeld on feature cannot have broader scope than FEATURE on feature: "
                        + feature.getDisplayName());
            }
            featureExplicitParamInjection = enableWeld.explicitParamInjection();
        } else if (disableWeldForFeature) {
            doEnableWeldForFeature = false;
            featureAutomagic = false;
            featureScope = null;
            featureExplicitParamInjection = false;
        } else {
            doEnableWeldForFeature = doEnableWeldForSpec;
            featureAutomagic = specAutomagic;
            featureScope = specScope;
            featureExplicitParamInjection = specExplicitParamInjection;
        }

        if (doEnableWeldForFeature) {
            EnableWeldInterceptor enableWeldInterceptorForFeature;

            // boot Weld around feature or iteration and inject shared fields
            switch (featureScope) {
                case SPECIFICATION:
                    enableWeldInterceptorForFeature = enableWeldInterceptorForSpec;
                    enableWeldInterceptorForFeature.handleFeature(feature);
                    break;

                case FEATURE:
                    enableWeldInterceptorForFeature = featureAutomagic
                            ? new EnableWeldAutoInterceptor(weldSpockEnrichers, featureExplicitParamInjection)
                            : new EnableWeldManualInterceptor(weldSpockEnrichers);
                    enableWeldInterceptorForFeature.handleFeature(feature);
                    feature.addInterceptor(enableWeldInterceptorForFeature);
                    break;

                case ITERATION:
                    enableWeldInterceptorForFeature = featureAutomagic
                            ? new EnableWeldAutoInterceptor(weldSpockEnrichers, featureExplicitParamInjection)
                            : new EnableWeldManualInterceptor(weldSpockEnrichers);
                    enableWeldInterceptorForFeature.handleFeature(feature);
                    feature.addIterationInterceptor(enableWeldInterceptorForFeature);
                    break;

                default:
                    throw new AssertionError();
            }

            // inject non-shared fields
            feature.addIterationInterceptor(enableWeldInterceptorForFeature.getTestInstanceInjector());

            // inject parameters for iteration methods
            Stream.Builder<MethodInfo> streamBuilder = Stream.builder();
            feature.getSpec().getSetupMethods().forEach(streamBuilder);
            streamBuilder.add(featureMethod);
            feature.getSpec().getCleanupMethods().forEach(streamBuilder);
            streamBuilder
                    .build()
                    .forEach(method -> attachParameterInjector(method, enableWeldInterceptorForFeature,
                            featureExplicitParamInjection));
        }
    }

    private void attachParameterInjector(MethodInfo method, EnableWeldInterceptor enableWeldInterceptor,
            boolean explicitParamInjection) {
        int amountOfParameters = method.getReflection().getParameters().length;
        int amountOfDataVariables = method.getFeature() == null ? 0 : method.getFeature().getDataVariables().size();
        // only attach if there could be injectable arguments
        if (amountOfParameters > amountOfDataVariables) {
            method.addInterceptor(enableWeldInterceptor.getParameterInjector(explicitParamInjection));
        }
    }
}
