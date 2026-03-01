/*
 * Copyright 2024 Aristide Cittadino
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.water.gradle.plugins.workspace.pin;

import groovy.lang.Closure;
import org.gradle.api.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DSL container for module-level property declarations inside a {@code waterDescriptor { properties { } }} block.
 *
 * <pre>
 * waterDescriptor {
 *     properties {
 *         property('it.water.user.registration.enabled') {
 *             required     = false
 *             sensitive    = false
 *             defaultValue = 'false'
 *             description  = 'Enables/disables user self-registration'
 *         }
 *     }
 * }
 * </pre>
 */
public class ModulePropertiesContainer {

    private final List<PinPropertySpec> properties = new ArrayList<>();

    /**
     * Declares a module-level configuration property.
     */
    public void property(String key, Action<PinPropertySpec> action) {
        PinPropertySpec spec = new PinPropertySpec(key);
        action.execute(spec);
        properties.add(spec);
    }

    /** Groovy DSL overload — closure delegate is set to the new {@link PinPropertySpec}. */
    public void property(String key, Closure<?> closure) {
        PinPropertySpec spec = new PinPropertySpec(key);
        ClosureConfigurer.configure(spec, closure);
        properties.add(spec);
    }

    public List<PinPropertySpec> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}
