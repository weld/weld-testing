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

import java.util.List;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.spock.EnableWeld;
import org.jboss.weld.spock.WeldInitiator;
import org.jboss.weld.spock.WeldSetup;
import org.jboss.weld.spock.WeldSpockEnricher;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import spock.lang.Shared;
import spock.lang.Specification;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * A Spock interceptor that is used for manual configuration of the booted Weld container.
 *
 * <p>If the interceptor is for scope {@code SPECIFICATION}, or for a data-driven feature with scope {@code FEATURE},
 * {@link Shared @Shared} fields of the specification and its super specifications are searched for exactly one
 * field that is annotated with {@link WeldSetup @WeldSetup}.
 *
 * <p>If multiple such fields are found, an exception is thrown.
 *
 * <p>If exactly one is found, and its value is not of type {@link WeldInitiator}, an exception is thrown.
 *
 * <p>Otherwise, as the type is correct, it is used as-is to initialize the Weld container.
 *
 * <p>If no field with that annotation is found, a new Weld initiator is created and the package of the test
 * class is added as beans to the container. Then all discovered {@link WeldSpockEnricher}s are applied to that instance
 * and the result is used to initialize the Weld container.
 *
 * @author Björn Kautler
 * @see EnableWeld
 * @see WeldSpockEnricher
 */
class EnableWeldManualInterceptor extends EnableWeldInterceptor {
    public EnableWeldManualInterceptor(List<WeldSpockEnricher> weldSpockEnrichers) {
        super(weldSpockEnrichers);
    }

    @Override
    protected WeldInitiator weldInit(IMethodInvocation invocation) {
        Specification spec = (Specification) invocation.getInstance();
        boolean shared = spec == invocation.getSharedInstance();

        List<FieldInfo> weldSetupFields = invocation
                .getSpec()
                .getAllFields()
                .stream()
                .filter(field -> (field.isShared() || field.isStatic()) == shared)
                .filter(field -> field.isAnnotationPresent(WeldSetup.class))
                .collect(toList());

        switch (weldSetupFields.size()) {
            case 0:
                Weld weld = WeldInitiator.createWeld();
                WeldInitiator.Builder builder = WeldInitiator.from(weld);

                weld.addPackage(false, invocation.getSpec().getReflection());

                // Apply discovered enrichers
                for (WeldSpockEnricher enricher : weldSpockEnrichers) {
                    String property = System.getProperty(enricher.getClass().getName());
                    if (property == null || Boolean.parseBoolean(property)) {
                        enricher.enrich(shared ? null : spec, weld, builder);
                    }
                }

                return builder.build();

            case 1:
                FieldInfo weldSetupField = weldSetupFields.get(0);
                Object weldSetupCandidate = weldSetupField.readValue(spec);
                if (!(weldSetupCandidate instanceof WeldInitiator)) {
                    throw new InvalidSpecException(format(
                            "@WeldSetup annotation should only be used on a field with a "
                            + "WeldInitiator value but was found on field %s with a "
                            + "%s value which is declared in %s",
                            weldSetupField.getName(),
                            ((weldSetupCandidate == null) ? "null" : weldSetupCandidate.getClass()),
                            weldSetupField.getParent().getDisplayName()));
                }
                return (WeldInitiator) weldSetupCandidate;

            default:
                throw new InvalidSpecException(weldSetupFields
                        .stream()
                        .map(f -> format("Field '%s' with type %s which is in %s", f.getName(), f.getType(), f.getParent().getDisplayName()))
                        .collect(joining("\n", "Multiple @WeldSetup annotated fields found, only one is allowed! Fields found:\n", "")));
        }
    }
}
