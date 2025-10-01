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
package org.jboss.weld.junit.jupiter;

import java.util.List;

import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * <b>This class is not a public API and it's contents can change unpredictably!</b>
 *
 * It contains utility methods for fetching/retrieving items stored in the JUnit extension context store.
 */
public class ExtensionContextUtils {

    // variables used to identify object in Store
    private static final String INITIATOR = "weldInitiator";
    private static final String CONTAINER = "weldContainer";
    private static final String EXPLICIT_PARAM_INJECTION = "explicitParamInjection";
    private static final String WELD_ENRICHERS = "weldEnrichers";

    private static Namespace EXTENSION_NAMESPACE;

    // private constructor to prevent instantiation
    private ExtensionContextUtils() {
    }

    /**
     * We use custom namespace based on this extension class; can be stored as static variable as it doesn't change throughout
     * testsuite execution.
     *
     * @param context {@link ExtensionContext} you are currently using
     * @return <b>Root</b> {@link ExtensionContext.Store} with {@link Namespace} based on extension class alone
     */
    private static synchronized ExtensionContext.Store getRootExtensionStore(ExtensionContext context) {
        if (EXTENSION_NAMESPACE == null) {
            EXTENSION_NAMESPACE = Namespace.create(WeldJUnitJupiterExtension.class);
        }
        return context.getRoot().getStore(EXTENSION_NAMESPACE);
    }

    /**
     * We use custom namespace based on this extension class and test class, cannot be stored as static variable as test class
     * name changes throughout testsuite execution
     *
     * @param context {@link ExtensionContext} you are currently using
     * @return {@link ExtensionContext.Store} based on {@link ExtensionContext} and the required test class
     */
    private static ExtensionContext.Store getTestStore(ExtensionContext context) {
        return context.getStore(Namespace.create(WeldJUnitJupiterExtension.class, context.getRequiredTestClass()));
    }

    /**
     * Can return null if WeldInitiator isn't stored yet
     */
    public static WeldInitiator getInitiatorFromStore(ExtensionContext context) {
        return getTestStore(context).get(INITIATOR, WeldInitiator.class);
    }

    /**
     * Stores {@link WeldInitiator} into provided {@link ExtensionContext.Store} based on provided {@link ExtensionContext}
     */
    public static void setInitiatorToStore(ExtensionContext context, WeldInitiator initiator) {
        getTestStore(context).put(INITIATOR, initiator);
    }

    /**
     * Return boolean indicating whether explicit parameter injection is enabled
     */
    public static Boolean getExplicitInjectionInfoFromStore(ExtensionContext context) {
        Boolean result = getTestStore(context).get(EXPLICIT_PARAM_INJECTION, Boolean.class);
        return (result == null) ? Boolean.FALSE : result;
    }

    /**
     * Store explicit injection parameter to {@link ExtensionContext.Store} based on provided {@link ExtensionContext}
     */
    public static void setExplicitInjectionInfoToStore(ExtensionContext context, boolean value) {
        getTestStore(context).put(EXPLICIT_PARAM_INJECTION, value);
    }

    /**
     * Can return null if WeldContainer isn't stored yet
     *
     * @param context {@link ExtensionContext} to search in
     * @return {@link WeldContainer} or null if it wasn't stored yet
     */
    public static WeldContainer getContainerFromStore(ExtensionContext context) {
        return getTestStore(context).get(CONTAINER, WeldContainer.class);
    }

    /**
     * Removes {@link WeldContainer} from {@link ExtensionContext.Store}.
     * <p>
     * This needs to be invoked before we complete shutdown otherwise JUnit (5.13+) attempts to close all autocloseable
     * resources which results in it trying to shut down Weld SE container that is no longer running.
     */
    public static void removeContainerFromStore(ExtensionContext context) {
        getTestStore(context).remove(CONTAINER);
    }

    /**
     * Store {@link WeldContainer} to {@link ExtensionContext.Store}
     */
    public static void setContainerToStore(ExtensionContext context, WeldContainer container) {
        getTestStore(context).put(CONTAINER, container);
    }

    /**
     * Can return null if `WeldJunitEnricher`s aren't stored yet.
     *
     * @param context {@link ExtensionContext} to search in
     * @return {@code List<WeldJunitEnricher>} or null in case they weren't stored yet
     */
    public static List<WeldJunitEnricher> getEnrichersFromStore(ExtensionContext context) {
        @SuppressWarnings("unchecked")
        List<WeldJunitEnricher> enrichers = (List<WeldJunitEnricher>) getRootExtensionStore(context).get(WELD_ENRICHERS,
                List.class);
        return enrichers;
    }

    /**
     * Store `WeldJunitEnricher`s to <i>root</i> extension context
     */
    public static void setEnrichersToStore(ExtensionContext context, List<WeldJunitEnricher> enrichers) {
        getRootExtensionStore(context).put(WELD_ENRICHERS, enrichers);
    }

}
