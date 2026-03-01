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

/**
 * Describes a single property belonging to a Water PIN.
 */
public class PinPropertySpec {

    private String key;
    private boolean required = true;
    private boolean sensitive = false;
    private String defaultValue = "";
    private String description = "";
    private String type = "string";
    private String envVar = "";

    public PinPropertySpec(String key) {
        this.key = key;
    }

    public String getKey() { return key; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEnvVar() { return envVar; }
    public void setEnvVar(String envVar) { this.envVar = envVar; }
}
