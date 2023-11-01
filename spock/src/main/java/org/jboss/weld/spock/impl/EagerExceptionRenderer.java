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

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.spockframework.runtime.SpockAssertionError;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

/**
 * A global Spock extension that eagerly renders exceptions, otherwise {@code toString()} might be called
 * on a CDI proxy when the container is shut down already and the call cannot be done.
 *
 * <p>
 * If in a power assertion output you see something like
 * {@code Something$Proxy$_$$_WeldClientProxy@4303056f (renderer threw IllegalStateException)},
 * this extension might miss some places where to eagerly render the exception, and you should
 * open an issue with a reproducer.
 *
 * @author Björn Kautler
 */
public class EagerExceptionRenderer implements IGlobalExtension {
    @Override
    public void visitSpec(SpecInfo spec) {
        Stream
                .concat(
                        StreamSupport.stream(spec.getAllFixtureMethods().spliterator(), false),
                        spec.getAllFeatures().stream().map(FeatureInfo::getFeatureMethod))
                .forEach(method -> method.addInterceptor(invocation -> {
                    try {
                        invocation.proceed();
                    } catch (SpockAssertionError spockAssertionError) {
                        spockAssertionError.toString();
                        throw spockAssertionError;
                    }
                }));
    }
}
