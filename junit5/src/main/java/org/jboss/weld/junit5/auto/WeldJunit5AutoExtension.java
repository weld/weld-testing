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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import static org.jboss.weld.junit5.ExtensionContextUtils.getExplicitInjectionInfoFromStore;

public class WeldJunit5AutoExtension extends org.jboss.weld.junit5.WeldJunit5Extension {

    @Override
    protected void weldInit(Object testInstance, ExtensionContext context, Weld weld, WeldInitiator.Builder weldInitiatorBuilder) {

        Class<?> testClass = testInstance.getClass();

        weld.addAlternativeStereotype(OverrideBean.class);

        ClassScanning.scanForRequiredBeanClass(testClass, weld, getExplicitInjectionInfoFromStore(context));

        weld.addBeanClass(testClass);
        weld.addExtension(new TestInstanceInjectionExtension(testClass, testInstance));

        AnnotationSupport.findRepeatableAnnotations(testClass, ActivateScopes.class)
                .forEach(ann -> weldInitiatorBuilder.activate(ann.value()));

    }

}
