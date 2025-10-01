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
package org.jboss.weld.junit.jupiter.auto;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.jboss.weld.junit.jupiter.ExtensionContextUtils.getExplicitInjectionInfoFromStore;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldJunitEnricher;
import org.jboss.weld.junit.jupiter.WeldJUnitJupiterExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * An alternative to {@link WeldJUnitJupiterExtension} allowing to fully leverage an annotation based configuration approach.
 * When used, the extension will attempt to resolve all beans used in your test class and automatically adds them to
 * the Weld container while bootstrapping it.
 *
 * <p>
 * There are quite some annotations which can be used to configure it further.
 *
 * <p>
 * Furthermore, all discovered {@link WeldJunitEnricher}s are invoked after the annotations are processed.
 *
 * <p>
 * Note that this approach cannot be combined with {@link WeldJUnitJupiterExtension}, choose one or the other approach, not both.
 *
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
 * @see EnableAutoWeld
 * @see SetBeanDiscoveryMode
 * @see WeldJunitEnricher
 */
public class WeldJUnitJupiterAutoExtension extends WeldJUnitJupiterExtension {
    @Override
    protected void validateInitiator(List<Field> foundInitiatorFields) {
        if (foundInitiatorFields.size() > 0) {
            throw new IllegalStateException(foundInitiatorFields
                    .stream()
                    .map(f -> format("Field '%s' with type %s which is in %s", f.getName(), f.getType(), f.getDeclaringClass()))
                    .collect(joining("\n",
                            "When using automagic mode, no @WeldSetup annotated field should be present! Fields found:\n",
                            "")));
        }
    }

    @Override
    protected void weldInit(ExtensionContext context, Weld weld, WeldInitiator.Builder weldInitiatorBuilder) {

        List<?> testInstances = context.getRequiredTestInstances().getAllInstances();
        List<Class<?>> testClasses = testInstances.stream().map(Object::getClass).collect(Collectors.toList());

        ClassScanning.scanForRequiredBeanClasses(testClasses, weld, getExplicitInjectionInfoFromStore(context));

        // Add the outer-most test class only because Weld would ignore inner, @Nested test classes anyway
        // due to their not meeting valid beans requirements for not having a no-arg constructor.
        // Note that getAllInstances above returns the tests "ordered from outermost to innermost".
        Object outermostTestInstance = testInstances.get(0);
        weld.addBeanClasses(outermostTestInstance.getClass());
        weld.addExtension(new TestInstanceInjectionExtension<>(outermostTestInstance));

        testClasses.stream()
                .map(testClass -> AnnotationSupport.findRepeatableAnnotations(testClass, ActivateScopes.class))
                .flatMap(ann -> ann.stream().map(ActivateScopes::value))
                .forEach(weldInitiatorBuilder::activate);

    }

}
