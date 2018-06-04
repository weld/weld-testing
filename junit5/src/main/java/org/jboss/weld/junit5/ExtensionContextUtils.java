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
package org.jboss.weld.junit5;

import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.util.List;


/**
 * Utility methods for fetching/storing items stored in the JUnit extension context.
 */
public class ExtensionContextUtils {

    // variables used to identify object in Store
    private static final String INITIATOR = "weldInitiator";
    private static final String CONTAINER = "weldContainer";
    private static final String EXPLICIT_PARAM_INJECTION = "explicitParamInjection";
    private static final String WELD_ENRICHERS = "weldEnrichers";

    private static Namespace EXTENSION_NAMESPACE;
    private static Namespace TEST_NAMESPACE;

    /**
     * We use custom namespace based on this extension class
     */
    public static ExtensionContext.Store getExtensionStore(ExtensionContext context) {
        if (EXTENSION_NAMESPACE == null) {
            EXTENSION_NAMESPACE = Namespace.create(WeldJunit5Extension.class);
        }
        return context.getStore(EXTENSION_NAMESPACE);
    }

    /**
     * We use custom namespace based on this extension class and test class
     */
    public static synchronized ExtensionContext.Store getTestStore(ExtensionContext context) {
        if (TEST_NAMESPACE == null) {
            TEST_NAMESPACE = Namespace.create(WeldJunit5Extension.class, context.getRequiredTestClass());
        }
        return context.getStore(TEST_NAMESPACE);
    }

    /**
     * Can return null if WeldInitiator isn't stored yet
     */
    public static WeldInitiator getInitiatorFromStore(ExtensionContext context) {
        return getTestStore(context).get(INITIATOR, WeldInitiator.class);
    }

    /**
     * Store initiator to extension context
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
     * Store explicit injection parameter to extension context
     */
    public static void setExplicitInjectionInfoToStore(ExtensionContext context, boolean value) {
        getTestStore(context).put(EXPLICIT_PARAM_INJECTION, value);
    }

    /**
     * Can return null if WeldContainer isn't stored yet
     */
    public static WeldContainer getContainerFromStore(ExtensionContext context) {
        return getTestStore(context).get(CONTAINER, WeldContainer.class);
    }

    /**
     * Store container to extension context
     */
    public static void setContainerToStore(ExtensionContext context, WeldContainer container) {
        getTestStore(context).put(CONTAINER, container);
    }

    /**
     * Can return null if `WeldJunitEnricher`s aren't stored yet.
     */
    public static List<WeldJunitEnricher> getEnrichersFromStore(ExtensionContext context) {
        @SuppressWarnings("unchecked")
        List<WeldJunitEnricher> enrichers = (List<WeldJunitEnricher>) getExtensionStore(context).get(WELD_ENRICHERS, List.class);
        return enrichers;
    }

    /**
     * Store `WeldJunitEnricher`s to extension context
     */
    public static void setEnrichersToStore(ExtensionContext context, List<WeldJunitEnricher> enrichers) {
        getExtensionStore(context).put(WELD_ENRICHERS, enrichers);
    }

}
