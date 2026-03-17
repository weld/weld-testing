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
package org.jboss.weld.junit.jupiter.bean.extension;

import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.junit.jupiter.EnableWeld;
import org.jboss.weld.junit.jupiter.WeldInitiator;
import org.jboss.weld.junit.jupiter.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@EnableWeld
public class AddExtensionAsBeanClassWithOfMethodTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.of(GoodOldBean.class, MyExtension.class);

    @Test
    public void testThatClassIsRecognizedAsExtension() {
        // GoodOldBean should be resolvable
        Assertions.assertTrue(weld.select(GoodOldBean.class).isResolvable());
        WeldInstance<MyExtension> extInstance = weld.select(MyExtension.class);
        // extension should be resolvable and should have observed the other bean
        Assertions.assertTrue(extInstance.isResolvable());
        Assertions.assertTrue(extInstance.get().beanObserved());
    }
}
