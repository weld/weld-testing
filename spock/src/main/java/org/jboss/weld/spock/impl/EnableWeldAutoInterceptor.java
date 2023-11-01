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

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.weld.spock.impl.ClassScanning.scanForRequiredBeanClasses;

import java.util.Arrays;
import java.util.List;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.spock.EnableWeld;
import org.jboss.weld.spock.WeldInitiator;
import org.jboss.weld.spock.WeldSetup;
import org.jboss.weld.spock.WeldSpockEnricher;
import org.jboss.weld.spock.auto.ActivateScopes;
import org.jboss.weld.spock.auto.AddBeanClasses;
import org.jboss.weld.spock.auto.AddEnabledDecorators;
import org.jboss.weld.spock.auto.AddEnabledInterceptors;
import org.jboss.weld.spock.auto.AddExtensions;
import org.jboss.weld.spock.auto.AddPackages;
import org.jboss.weld.spock.auto.EnableAlternativeStereotypes;
import org.jboss.weld.spock.auto.EnableAlternatives;
import org.jboss.weld.spock.auto.ExcludeBean;
import org.jboss.weld.spock.auto.ExcludeBeanClasses;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.Specification;

/**
 * An alternative to {@link EnableWeldManualInterceptor} allowing to fully leverage an annotation based configuration approach.
 * When selected by {@link EnableWeld#automagic()}, this interceptor will attempt to resolve all beans used in the test class
 * and automatically adds them to the Weld container while bootstrapping it.
 *
 * <p>
 * There are quite some annotations which can be used on the test class and on any discovered bean to configure it further.
 *
 * <p>
 * Having a {@link WeldSetup @WeldSetup} annotated field in the specification or a super specification results in an
 * exception, because it would not be considered and thus is a sign that the manual interceptor should be used, or that
 * the field is a left-over from switching and should be removed.
 *
 * <p>
 * Furthermore, all discovered {@link WeldSpockEnricher}s are invoked after the annotations are processed.
 *
 * @author Björn Kautler
 * @see ActivateScopes
 * @see AddBeanClasses
 * @see AddEnabledDecorators
 * @see AddEnabledInterceptors
 * @see AddExtensions
 * @see AddPackages
 * @see EnableAlternatives
 * @see EnableAlternativeStereotypes
 * @see ExcludeBean
 * @see ExcludeBeanClasses
 * @see EnableWeld
 * @see WeldSpockEnricher
 */
class EnableWeldAutoInterceptor extends EnableWeldInterceptor {
    private final boolean explicitParamInjection;

    public EnableWeldAutoInterceptor(List<WeldSpockEnricher> weldSpockEnrichers, boolean explicitParamInjection) {
        super(weldSpockEnrichers);
        this.explicitParamInjection = explicitParamInjection;
    }

    @Override
    protected WeldInitiator weldInit(IMethodInvocation invocation) {
        Specification testInstance = (Specification) invocation.getInstance();

        SpecInfo spec = invocation.getSpec();
        List<FieldInfo> weldSetupFields = spec
                .getAllFields()
                .stream()
                .filter(field -> field.isAnnotationPresent(WeldSetup.class))
                .collect(toList());

        if (weldSetupFields.size() > 0) {
            throw new InvalidSpecException(weldSetupFields
                    .stream()
                    .map(f -> format("Field '%s' with type %s which is in %s", f.getName(), f.getType(),
                            f.getParent().getDisplayName()))
                    .collect(joining("\n",
                            "When using automagic mode, no @WeldSetup annotated field should be present! Fields found:\n",
                            "")));
        }

        Weld weld = WeldInitiator.createWeld();
        WeldInitiator.Builder builder = WeldInitiator.from(weld);

        scanForRequiredBeanClasses(spec.getReflection(), weld, explicitParamInjection);

        weld.addBeanClasses(spec.getReflection());
        weld.addExtension(new TestInstanceInjectionExtension<>(testInstance));

        spec
                .getSpecsBottomToTop()
                .stream()
                .map(s -> s.getAnnotationsByType(ActivateScopes.class))
                .flatMap(Arrays::stream)
                .map(ActivateScopes::value)
                .flatMap(Arrays::stream)
                .forEach(builder::activate);

        // Apply discovered enrichers
        for (WeldSpockEnricher enricher : weldSpockEnrichers) {
            String property = System.getProperty(enricher.getClass().getName());
            if (property == null || Boolean.parseBoolean(property)) {
                enricher.enrich((testInstance == invocation.getSharedInstance()) ? null : testInstance, weld, builder);
            }
        }

        return builder.build();
    }
}
