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
package org.jboss.weld.junit5.auto;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldJunitEnricher;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import static org.jboss.weld.junit5.ExtensionContextUtils.getExplicitInjectionInfoFromStore;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An alternative to {@link WeldJunit5Extension} allowing to fully leverage annotation based configuration approach.
 * When used, the extension will attempt to resolve all beans used in your test class and automatically adds them to
 * Weld container while bootstrapping it.
 * 
 * There is quite a few annotations which can be used to configure it further still:
 * @see ActivateScopes
 * @see AddBeanClasses
 * @see AddEnabledDecorators
 * @see AddEnabledInterceptors
 * @see AddExtensions
 * @see AddPackages
 * @see EnableAlternatives
 * @see EnableAlternativeStereotypes
 *
 * Note that this approach cannot be combined with {@link WeldJunit5Extension}, choose one or the other approach, not both.
 *
 * @see EnableAutoWeld
 * @see WeldJunitEnricher
 */
public class WeldJunit5AutoExtension extends WeldJunit5Extension {

    @Override
    protected void weldInit(ExtensionContext context, Weld weld, WeldInitiator.Builder weldInitiatorBuilder) {

        List<?> testInstances = context.getRequiredTestInstances().getAllInstances();
        List<Class<?>> testClasses = testInstances.stream().map(Object::getClass).collect(Collectors.toList());

        ClassScanning.scanForRequiredBeanClasses(testClasses, weld, getExplicitInjectionInfoFromStore(context));

        weld.addExtension(new TestInstanceInjectionExtension(testInstances));

        for (Class<?> testClass : testClasses) {
            AnnotationSupport.findRepeatableAnnotations(testClass, ActivateScopes.class)
                    .forEach(ann -> weldInitiatorBuilder.activate(ann.value()));
        }

    }

}
