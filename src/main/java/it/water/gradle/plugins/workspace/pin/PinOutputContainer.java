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
import org.gradle.api.GradleException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DSL container for output PIN declarations inside a {@code waterDescriptor { output { } }} block.
 */
public class PinOutputContainer {

    private final List<OutputPinSpec> pins = new ArrayList<>();

    /**
     * Declares a custom output PIN with an explicit property schema.
     * <pre>
     * output {
     *     pin('it.water.integration.authentication-issuer') {
     *         property('water.authentication.service.issuer') {
     *             required     = true
     *             defaultValue = 'water'
     *         }
     *     }
     * }
     * </pre>
     */
    public void pin(String id, Action<OutputPinSpec> action) {
        OutputPinSpec spec = new OutputPinSpec(id);
        ClosureConfigurer.configure(spec, action);
        pins.add(spec);
    }

    /** Groovy DSL overload — closure delegate is set to the new {@link OutputPinSpec}. */
    public void pin(String id, Closure<?> closure) {
        OutputPinSpec spec = new OutputPinSpec(id);
        ClosureConfigurer.configure(spec, closure);
        pins.add(spec);
    }

    /**
     * Declares a standard output PIN from the built-in catalog.
     * <pre>
     * output { standardPin 'jdbc' }
     * </pre>
     */
    public void standardPin(String shorthand) {
        OutputPinSpec spec = StandardPins.get(shorthand);
        if (spec == null)
            throw new GradleException("Unknown standard Water PIN: '" + shorthand + "'. " +
                    "Available: jdbc, api-gateway, service-discovery, cluster-coordinator, authentication-issuer");
        pins.add(spec);
    }

    /**
     * Declares a standard output PIN extended with extra module-specific properties.
     * <pre>
     * output {
     *     standardPin('jdbc') {
     *         property('db.schema') { required = false; defaultValue = 'public' }
     *     }
     * }
     * </pre>
     */
    public void standardPin(String shorthand, Action<OutputPinSpec> action) {
        OutputPinSpec spec = StandardPins.get(shorthand);
        if (spec == null)
            throw new GradleException("Unknown standard Water PIN: '" + shorthand + "'. " +
                    "Available: jdbc, api-gateway, service-discovery, cluster-coordinator, authentication-issuer");
        ClosureConfigurer.configure(spec, action);
        pins.add(spec);
    }

    /** Groovy DSL overload — closure delegate is set to the catalog copy. */
    public void standardPin(String shorthand, Closure<?> closure) {
        OutputPinSpec spec = StandardPins.get(shorthand);
        if (spec == null)
            throw new GradleException("Unknown standard Water PIN: '" + shorthand + "'. " +
                    "Available: jdbc, api-gateway, service-discovery, cluster-coordinator, authentication-issuer");
        ClosureConfigurer.configure(spec, closure);
        pins.add(spec);
    }

    public List<OutputPinSpec> getPins() {
        return Collections.unmodifiableList(pins);
    }
}
