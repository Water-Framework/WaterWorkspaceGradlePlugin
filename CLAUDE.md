# WaterWorkspaceGradlePlugin — Build Orchestration Gradle Plugin

## Purpose
A custom Gradle plugin that manages the Water Framework's multi-module workspace build system. Handles version alignment across all modules, build orchestration, multi-level project hierarchies, and publishing to Maven repositories. Applied to the root `settings.gradle` to configure the entire workspace.

## Key Responsibilities

1. **Version alignment** — ensures all Water modules use the same `3.0.0` (or configured) version
2. **Multi-project hierarchy support** — discovers and configures nested sub-projects automatically
3. **Build orchestration** — defines task dependencies across module boundaries
4. **Publishing configuration** — unified `publishToMavenLocal` and `publish` configuration for all sub-modules
5. **Workspace conventions** — enforces project naming conventions, source set layout, dependency resolution

## Plugin Structure

```
WaterWorkspaceGradlePlugin/
├─ src/main/groovy/it/water/gradle/
│   ├─ WaterWorkspacePlugin.groovy          # main plugin entry point
│   ├─ WaterModulePlugin.groovy             # applied to each sub-module
│   ├─ WaterPublishingPlugin.groovy         # publishing configuration
│   └─ WaterVersionPlugin.groovy            # version management
├─ src/test/
│   └─ testProjects/
│       ├─ TestProject/
│       │   └─ TestSubProject/
│       │       └─ TestSubSubProject/       # 3-level hierarchy test
│       └─ Test2Project/
│           └─ Test2SubProject/
│               └─ Test2SubSubProject/      # 3-level hierarchy test (variant)
└─ build.gradle                             # plugin self-build
```

## Application

Applied in the root `settings.gradle`:
```groovy
plugins {
    id 'it.water.workspace' version '3.0.0'
}

// All sub-projects are auto-discovered
waterWorkspace {
    version = '3.0.0'
    groupId = 'it.water'
    publishUrl = 'nexus.acsoftware.it/nexus/repository/maven-water/'
}
```

## WaterWorkspacePlugin Behaviors

### Auto-discovery of sub-projects
Recursively scans the workspace root for directories containing `build.gradle` files and registers them as Gradle sub-projects — eliminating the need to manually list them in `settings.gradle`.

### Version Alignment
All Water modules inherit the root workspace version:
```groovy
// Applied to every sub-project automatically
subprojects {
    version = rootProject.waterWorkspace.version
    group   = rootProject.waterWorkspace.groupId
}
```

### Common Dependency Management
Provides a BOM-like dependency management block that pins all Water module versions:
```groovy
// Auto-added to every sub-project
dependencyManagement {
    imports {
        mavenBom "it.water:water-bom:${waterVersion}"
    }
}
```

### Publishing Convention
All sub-modules get a unified `water` Maven publication:
```groovy
publishing {
    publications {
        water(MavenPublication) {
            from components.java
            // POM metadata auto-populated from project name + description
        }
    }
    repositories {
        maven { url = waterWorkspace.publishUrl }
    }
}
```

## Test Projects

The `testProjects/` directory validates the plugin works correctly for 3-level project hierarchies:

```
TestProject/                          # Level 1: top-level module
  ├─ build.gradle
  └─ TestSubProject/                  # Level 2: sub-module
       ├─ build.gradle
       └─ TestSubSubProject/          # Level 3: sub-sub-module
            └─ build.gradle
```

Tests verify:
- All 3 levels are discovered and registered as Gradle projects
- Version propagated correctly to all levels
- `publishToMavenLocal` works on leaf projects
- Build dependency ordering respected

## Common Gradle Tasks (from the plugin)

```bash
# Build a specific module + dependencies
./gradlew :Core:Core-api:build

# Publish all modules to local Maven
./gradlew publishToMavenLocal

# Publish to remote (Nexus)
./gradlew publish

# List all discovered projects
./gradlew projects

# Check version alignment
./gradlew :checkVersionAlignment
```

## Dependencies
- Gradle 7.6+ API (`org.gradle:gradle-api`)
- Groovy (bundled with Gradle)
- No Water framework runtime dependencies — this is a build-time tool only

## Testing
```bash
# Run plugin unit tests
./gradlew WaterWorkspaceGradlePlugin:test

# Integration test with nested project hierarchy
./gradlew WaterWorkspaceGradlePlugin:integrationTest
```

## Code Generation Rules
- This plugin is build infrastructure — changes here affect ALL modules in the workspace
- Always run the full test suite (`./gradlew WaterWorkspaceGradlePlugin:test`) after any plugin change before applying it to the workspace
- New conventions (source set layouts, dependency rules): add to `WaterModulePlugin`, not to individual module `build.gradle` files
- `testProjects/` must be updated when adding new hierarchy levels or plugin features
- Version bumps: change ONLY in the root `waterWorkspace.version` — the plugin propagates it everywhere
- `publishUrl` is parameterized — never hardcode Nexus URLs in module `build.gradle` files
