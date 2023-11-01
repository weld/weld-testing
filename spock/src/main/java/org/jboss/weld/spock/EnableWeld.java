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

package org.jboss.weld.spock;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.jboss.weld.spock.EnableWeld.Scope.ITERATION;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.weld.spock.impl.EnableWeldExtension;

import spock.lang.Shared;

/**
 * An annotation with which Weld can be enabled on specification or feature level if it was
 * disabled globally or on specification level.
 *
 * <p>
 * The exact effect of this annotation depends on the further properties set on it as described
 * in the documentation of the individual properties.
 *
 * <p>
 * On any class that is not a specification and on any method that is not a feature, this annotation
 * is simply ignored and has no effect.
 *
 * <p>
 * If this annotation is applied on the same element as {@link DisableWeld @DisableWeld}, an exception
 * is thrown as it is unclear which should have precedence.
 *
 * @author Björn Kautler
 * @see DisableWeld
 * @see EnableWeldExtension
 * @see org.jboss.weld.spock.impl.EnableWeldManualInterceptor
 * @see org.jboss.weld.spock.impl.EnableWeldAutoInterceptor
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Documented
public @interface EnableWeld {
    /**
     * Whether to use the {@link org.jboss.weld.spock.impl.EnableWeldManualInterceptor} ({@code false}) with which you mostly
     * manually configure Weld and have full control over which beans make it into the container, or the
     * {@link org.jboss.weld.spock.impl.EnableWeldAutoInterceptor} with which most things should happen automagically
     * but can be adjusted using annotations. See those classes' documentation and also {@link EnableWeldExtension} for further
     * information.
     *
     * @return whether to use automagic or manual logic
     */
    boolean automagic() default false;

    /**
     * The scope for which the Weld container should be valid and used. See the documentation of the {@code Scope} values
     * for details about the individual values.
     *
     * @return the scope for which the Weld container should be valid and used
     */
    Scope scope() default ITERATION;

    /**
     * Whether to require explicit parameter injection. By default, all method parameters that are no data variables
     * and are not already set by another Spock extension are checked whether they can be resolved from the Weld container.
     * If this is the case, they are automatically injected, otherwise they are ignored, to be filled by a later
     * running Spock extension. Using this parameter, this automatic behavior can be disabled and instead require
     * that each parameter that should be injected has a qualifier annotation, like for example {@code @Default}.
     *
     * <p>
     * If explicit parameter injection is enabled and a parameter that is not yet injected by another Spock extension
     * but has a qualifier annotation cannot be resolved using the Weld container, an exception will be thrown.
     *
     * @return whether to require explicit parameter injection
     */
    boolean explicitParamInjection() default false;

    /**
     * The scope for which the Weld container should be valid and used.
     */
    enum Scope {
        /**
         * Before an iteration (before the {@code setup} method is called) a new Weld container will be started,
         * exclusively used for this iteration and after the iteration (after the {@code cleanup} method is called)
         * shut down again.
         *
         * <p>
         * The {@link Shared @Shared} fields will be injected, but only at the time the iteration starts running.
         * If you have multiple such Weld instances in one specification and use the parallel execution feature of Spock,
         * those Weld containers might overwrite each other's values, so be careful what you configure to not get unexpected
         * results.
         *
         * <p>
         * The non-{@code @Shared} fields will also be injected, as well as method parameters of {@code setup},
         * feature, and {@code cleanup} methods.
         *
         * <p>
         * {@code setupSpec} and {@code cleanupSpec} method parameters will not be injected as at the time those
         * methods are executed, the Weld container is either not yet running or already shut down.
         */
        ITERATION,

        /**
         * Before a feature (before the {@code setup} method of the first iteration is called) a new Weld container will be
         * started,
         * exclusively used for all iterations of this feature and after the feature (after the {@code cleanup} method of
         * the last iteration is called) shut down again.
         *
         * <p>
         * The {@link Shared @Shared} fields will be injected, but only at the time the first iteration starts running.
         * If you have multiple such Weld instances in one specification and use the parallel execution feature of Spock,
         * those Weld containers might overwrite each other's values, so be careful what you configure to not get unexpected
         * results.
         *
         * <p>
         * The non-{@code @Shared} fields will also be injected, as well as method parameters of {@code setup},
         * feature, and {@code cleanup} methods.
         *
         * <p>
         * {@code setupSpec} and {@code cleanupSpec} method parameters will not be injected as at the time those
         * methods are executed, the Weld container is either not yet running or already shut down.
         */
        FEATURE,

        /**
         * Before a specification (before the {@code setupSpec} method is called) a new Weld container will be started,
         * exclusively used for all iterations of all features of this specification and after the specification
         * (after the {@code cleanupSpec} method is called) shut down again.
         *
         * <p>
         * The {@link Shared @Shared} fields will be injected once after the container was booted successfully.
         *
         * <p>
         * The non-{@code @Shared} fields will also be injected, as well as method parameters of feature,
         * and all fixture methods.
         *
         * <p>
         * This scope can only be selected on a specification or in the Spock configuration file. If it is used
         * for a feature annotation, an exception will be thrown.
         */
        SPECIFICATION
    }
}
