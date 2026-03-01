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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Built-in catalog of standard Water Framework PINs.
 * Modules reference them via {@code standardPin 'shorthand'} in the DSL
 * instead of re-declaring all properties each time.
 */
public class StandardPins {

    private static final Map<String, OutputPinSpec> CATALOG;

    static {
        Map<String, OutputPinSpec> m = new LinkedHashMap<>();
        m.put("jdbc",                    jdbc());
        m.put("api-gateway",             apiGateway());
        m.put("service-discovery",       serviceDiscovery());
        m.put("cluster-coordinator",     clusterCoordinator());
        m.put("authentication-issuer",   authenticationIssuer());
        CATALOG = Collections.unmodifiableMap(m);
    }

    private StandardPins() {}

    /**
     * Returns a copy of the standard PIN spec for the given shorthand,
     * or {@code null} if the shorthand is not known.
     */
    public static OutputPinSpec get(String shorthand) {
        OutputPinSpec spec = CATALOG.get(shorthand);
        return spec != null ? spec.copy() : null;
    }

    // -------------------------------------------------------------------------
    // Catalog entries
    // -------------------------------------------------------------------------

    private static OutputPinSpec jdbc() {
        OutputPinSpec p = new OutputPinSpec("it.water.persistence.jdbc");
        p.setRequired(true);
        p.addProperty("db.host",      true,  false, "",     "Database hostname");
        p.addProperty("db.port",      true,  false, "5432", "Database port");
        p.addProperty("db.username",  true,  false, "",     "Database username");
        p.addProperty("db.password",  true,  true,  "",     "Database password");
        p.addProperty("db.pool.size", false, false, "10",   "Connection pool size");
        return p;
    }

    private static OutputPinSpec apiGateway() {
        OutputPinSpec p = new OutputPinSpec("it.water.api-gateway");
        p.setRequired(false);
        p.addProperty("gateway.base.url",       true,  false, "",      "API Gateway base URL");
        p.addProperty("gateway.admin.url",      false, false, "",      "API Gateway admin URL");
        p.addProperty("gateway.timeout.millis", false, false, "30000", "Connection timeout in milliseconds");
        return p;
    }

    private static OutputPinSpec serviceDiscovery() {
        OutputPinSpec p = new OutputPinSpec("it.water.service-discovery");
        p.setRequired(false);
        p.addProperty("service.name",                  true,  false, "",                  "Logical service name");
        p.addProperty("service.instance-id",           false, false, "",                  "Unique instance ID (auto UUID if empty)");
        p.addProperty("service.endpoint",              true,  false, "",                  "Service endpoint URL");
        p.addProperty("service.protocol",              false, false, "HTTP",               "Communication protocol");
        p.addProperty("service.health-check.endpoint", false, false, "/actuator/health",  "Health check endpoint path");
        p.addProperty("service.health-check.interval", false, false, "30",                "Health check interval in seconds");
        return p;
    }

    private static OutputPinSpec clusterCoordinator() {
        OutputPinSpec p = new OutputPinSpec("it.water.cluster.coordinator");
        p.setRequired(false);
        p.addProperty("it.water.connectors.zookeeper.url",       true,  false, "localhost:2181",          "Zookeeper ensemble connection string");
        p.addProperty("it.water.connectors.zookeeper.base.path", false, false, "/water-framework/layers", "Zookeeper base path for Water cluster data");
        p.addProperty("water.core.cluster.node.id",              true,  false, "",                        "Cluster node unique ID");
        p.addProperty("water.core.cluster.node.layer.id",        true,  false, "",                        "Cluster layer identifier");
        p.addProperty("water.core.cluster.node.host",            false, false, "",                        "Node hostname");
        p.addProperty("water.core.cluster.node.ip",              false, false, "",                        "Node IP address");
        p.addProperty("water.core.cluster.node.use-ip",          false, false, "false",                   "Use IP instead of hostname for cluster registration");
        p.addProperty("water.core.cluster.mode.enabled",         false, false, "false",                   "Enable cluster mode");
        return p;
    }

    private static OutputPinSpec authenticationIssuer() {
        OutputPinSpec p = new OutputPinSpec("it.water.integration.authentication-issuer");
        p.setRequired(true);
        p.addProperty("water.authentication.service.issuer", true, false, "water", "Issuer name for JWT tokens");
        return p;
    }
}
