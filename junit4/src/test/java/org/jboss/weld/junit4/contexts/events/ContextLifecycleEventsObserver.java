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
package org.jboss.weld.junit4.contexts.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class ContextLifecycleEventsObserver {

    static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    void onRequestContextInit(@Observes @Initialized(RequestScoped.class) Object event) {
        EVENTS.add(Initialized.class.getName() + RequestScoped.class.getName());
    }

    void onRequestContextDestroy(@Observes @Destroyed(RequestScoped.class) Object event) {
        EVENTS.add(Destroyed.class.getName() + RequestScoped.class.getName());
    }

    void onSessionContextInit(@Observes @Initialized(SessionScoped.class) Object event) {
        EVENTS.add(Initialized.class.getName() + SessionScoped.class.getName());
    }

    void onSessionContextDestroy(@Observes @Destroyed(SessionScoped.class) Object event) {
        EVENTS.add(Destroyed.class.getName() + SessionScoped.class.getName());
    }

}
