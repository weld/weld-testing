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
package org.jboss.weld.junit4.bean;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.context.Dependent;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class AlternativeMockBeanTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.from(SimpleService.class)
            .addBeans(MockBean.builder().types(MyService.class).selectedAlternative(CoolService.class)
                    .create(c -> new CoolService()).build())
            .build();

    @Test
    public void testAlternativeBeanSelected() {
        assertEquals(1000, weld.select(MyService.class).get().doBusiness());
    }

    interface MyService {

        int doBusiness();

    }

    @Dependent
    static class SimpleService implements MyService {

        @Override
        public int doBusiness() {
            return 0;
        }
    }

    static class CoolService implements MyService {

        @Override
        public int doBusiness() {
            return 1000;
        }
    }

}
