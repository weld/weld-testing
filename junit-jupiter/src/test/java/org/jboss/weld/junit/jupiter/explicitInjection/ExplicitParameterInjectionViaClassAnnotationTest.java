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
package org.jboss.weld.junit.jupiter.explicitInjection;

import jakarta.enterprise.inject.Default;

import org.jboss.weld.junit.jupiter.ExplicitParamInjection;
import org.jboss.weld.junit.jupiter.WeldJUnitJupiterExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@ExtendWith(WeldJUnitJupiterExtension.class)
@ExplicitParamInjection
public class ExplicitParameterInjectionViaClassAnnotationTest {

    @Test
    @ExtendWith(CustomExtension.class)
    public void testParametersNeedExtraAnnotation(@Default Foo foo, Bar bar, @MyQualifier BeanWithQualifier bean) {
        // Bar should be resolved by another extension
        Assertions.assertNotNull(bar);
        Assertions.assertEquals(CustomExtension.class.getSimpleName(), bar.ping());
        // Foo should be resolved as usual
        Assertions.assertNotNull(foo);
        Assertions.assertEquals(Foo.class.getSimpleName(), foo.ping());
        // BeanWithQualifier should be resolved
        Assertions.assertNotNull(bean);
        Assertions.assertEquals(BeanWithQualifier.class.getSimpleName(), bean.ping());
    }
}
