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

import org.jboss.weld.spock.EnableWeld.Scope;
import org.jboss.weld.spock.impl.EnableWeldExtension;
import spock.config.ConfigurationObject;

import static org.jboss.weld.spock.EnableWeld.Scope.ITERATION;

/**
 * Configuration settings for the weld-spock extensions.
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * import static org.jboss.weld.spock.EnableWeld.Scope.SPECIFICATION
 *
 * 'org.jboss.weld' {
 *   enabled true // default false
 *   automagic true // default false
 *   scope SPECIFICATION // default ITERATION
 *   explicitParamInjection true // default false
 * }
 * }</pre>
 *
 * @author Björn Kautler
 * @see EnableWeldExtension
 */
@ConfigurationObject("org.jboss.weld")
public class WeldConfiguration {
    public boolean enabled = false;
    public boolean automagic = false;
    public Scope scope = ITERATION;
    public boolean explicitParamInjection = false;
}
