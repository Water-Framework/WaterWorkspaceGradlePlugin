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
 * DSL container for input PIN declarations inside a {@code waterDescriptor { input { } }} block.
 */
public class PinInputContainer {

    private final List<InputPinSpec> pins = new ArrayList<>();

    /**
     * Declares a required input PIN (shorthand form).
     * <pre>
     * input { pin 'it.water.integration.authentication-issuer' }
     * </pre>
     */
    public void pin(String id) {
        pins.add(new InputPinSpec(id, true));
    }

    /**
     * Declares an input PIN with explicit options.
     * <pre>
     * input {
     *     pin('it.water.api.service-discovery') { required = false }
     * }
     * </pre>
     */
    public void pin(String id, Action<InputPinSpec> action) {
        InputPinSpec spec = new InputPinSpec(id, true);
        ClosureConfigurer.configure(spec, action);
        pins.add(spec);
    }

    /** Groovy DSL overload — closure delegate is set to the new {@link InputPinSpec}. */
    public void pin(String id, Closure<?> closure) {
        InputPinSpec spec = new InputPinSpec(id, true);
        ClosureConfigurer.configure(spec, closure);
        pins.add(spec);
    }

    /**
     * Declares a standard input PIN, inheriting the {@code required} hint from the catalog.
     * <pre>
     * input { standardPin 'jdbc' }
     * </pre>
     */
    public void standardPin(String shorthand) {
        OutputPinSpec base = StandardPins.get(shorthand);
        if (base == null)
            throw new GradleException("Unknown standard Water PIN: '" + shorthand + "'. " +
                    "Available: jdbc, api-gateway, service-discovery, cluster-coordinator, authentication-issuer");
        pins.add(new InputPinSpec(base.getId(), base.isRequired()));
    }

    /**
     * Declares a standard input PIN with an explicit {@code required} override.
     * <pre>
     * input {
     *     standardPin('service-discovery') { required = false }
     * }
     * </pre>
     */
    public void standardPin(String shorthand, Action<InputPinSpec> action) {
        OutputPinSpec base = StandardPins.get(shorthand);
        if (base == null)
            throw new GradleException("Unknown standard Water PIN: '" + shorthand + "'. " +
                    "Available: jdbc, api-gateway, service-discovery, cluster-coordinator, authentication-issuer");
        InputPinSpec spec = new InputPinSpec(base.getId(), base.isRequired());
        ClosureConfigurer.configure(spec, action);
        pins.add(spec);
    }

    /** Groovy DSL overload — closure delegate is set to the {@link InputPinSpec}. */
    public void standardPin(String shorthand, Closure<?> closure) {
        OutputPinSpec base = StandardPins.get(shorthand);
        if (base == null)
            throw new GradleException("Unknown standard Water PIN: '" + shorthand + "'. " +
                    "Available: jdbc, api-gateway, service-discovery, cluster-coordinator, authentication-issuer");
        InputPinSpec spec = new InputPinSpec(base.getId(), base.isRequired());
        ClosureConfigurer.configure(spec, closure);
        pins.add(spec);
    }

    public List<InputPinSpec> getPins() {
        return Collections.unmodifiableList(pins);
    }
}
