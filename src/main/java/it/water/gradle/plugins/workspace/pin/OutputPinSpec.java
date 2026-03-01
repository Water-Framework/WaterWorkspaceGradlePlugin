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
 * Describes an output PIN declared by a module — a named group of properties
 * that this module provides values for.
 */
public class OutputPinSpec {

    private final String id;
    private boolean required = false;
    private final List<PinPropertySpec> properties = new ArrayList<>();

    public OutputPinSpec(String id) {
        this.id = id;
    }

    /** DSL (Java/Kotlin): property('db.host') { required = true; sensitive = false } */
    public void property(String key, Action<PinPropertySpec> action) {
        PinPropertySpec spec = new PinPropertySpec(key);
        ClosureConfigurer.configure(spec, action);
        properties.add(spec);
    }

    /** Groovy DSL overload — closure delegate is set to the new {@link PinPropertySpec}. */
    public void property(String key, Closure<?> closure) {
        PinPropertySpec spec = new PinPropertySpec(key);
        ClosureConfigurer.configure(spec, closure);
        properties.add(spec);
    }

    /** Used internally by StandardPins to build the catalog. */
    void addProperty(String key, boolean required, boolean sensitive, String defaultValue, String description) {
        PinPropertySpec spec = new PinPropertySpec(key);
        spec.setRequired(required);
        spec.setSensitive(sensitive);
        spec.setDefaultValue(defaultValue);
        spec.setDescription(description);
        properties.add(spec);
    }

    /** Returns a deep copy so that the catalog entry is never mutated. */
    public OutputPinSpec copy() {
        OutputPinSpec copy = new OutputPinSpec(this.id);
        copy.required = this.required;
        for (PinPropertySpec p : this.properties) {
            PinPropertySpec pc = new PinPropertySpec(p.getKey());
            pc.setRequired(p.isRequired());
            pc.setSensitive(p.isSensitive());
            pc.setDefaultValue(p.getDefaultValue());
            pc.setDescription(p.getDescription());
            copy.properties.add(pc);
        }
        return copy;
    }

    public String getId() { return id; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public List<PinPropertySpec> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}
