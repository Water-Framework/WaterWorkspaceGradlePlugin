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

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes an input PIN declared by a module — a named group of properties
 * that this module requires to be satisfied by another module's output PIN.
 */
public class InputPinSpec {

    private final String id;
    private boolean required;
    private final List<PinPropertySpec> properties = new ArrayList<>();

    public InputPinSpec(String id, boolean required) {
        this.id = id;
        this.required = required;
    }

    public String getId() { return id; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    /** Copy properties from an OutputPinSpec (used when the same standard PIN is declared as input). */
    public void addProperties(List<PinPropertySpec> source) {
        for (PinPropertySpec p : source) {
            PinPropertySpec copy = new PinPropertySpec(p.getKey());
            copy.setName(p.getName());
            copy.setRequired(p.isRequired());
            copy.setSensitive(p.isSensitive());
            copy.setDefaultValue(p.getDefaultValue());
            copy.setDescription(p.getDescription());
            properties.add(copy);
        }
    }

    public List<PinPropertySpec> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}
