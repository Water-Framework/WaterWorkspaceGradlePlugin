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

/**
 * Helper that executes an {@link Action} or Groovy {@link Closure} with proper
 * delegate setup so that the idiomatic Gradle DSL works without explicit parameters:
 * <pre>
 *     pin('it.water.some.pin') {
 *         property('key') { required = true }   // ← resolved against OutputPinSpec
 *     }
 * </pre>
 *
 * <p>Gradle wraps Groovy closures in a {@code ClosureBackedAction} SAM adapter when
 * methods declare {@code Action<T>} parameters, so {@code action instanceof Closure}
 * is false at the Java level.  The solution is to provide Groovy-specific overloads
 * that accept {@link Closure} directly alongside the {@code Action<T>} API, and let
 * Groovy's method resolution pick the most specific match.
 */
final class ClosureConfigurer {

    private ClosureConfigurer() {}

    /**
     * Executes a plain {@link Action} against {@code target} (Java / Kotlin path).
     */
    static <T> void configure(T target, Action<T> action) {
        action.execute(target);
    }

    /**
     * Executes a Groovy {@link Closure} against {@code target} with the closure's
     * delegate set to {@code target} and resolve strategy {@code DELEGATE_FIRST}.
     * This is the Groovy DSL path.
     */
    @SuppressWarnings("unchecked")
    static <T> void configure(T target, Closure<?> closure) {
        Closure<Void> configured = (Closure<Void>) closure.rehydrate(
                target, closure.getOwner(), closure.getThisObject());
        configured.setResolveStrategy(Closure.DELEGATE_FIRST);
        configured.call(target);
    }
}
