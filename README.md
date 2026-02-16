# WaterWorkspaceGradlePlugin

The **WaterWorkspaceGradlePlugin** is a Gradle **Settings** plugin that automates workspace management for Water Framework projects. It handles automatic project discovery, dependency management, custom configurations for JAR packaging, and Karaf features generation.

## Features

- **Automatic project discovery** — Recursively finds all `build.gradle` files and includes them in the workspace
- **Custom JAR configurations** — `implementationInclude` and `implementationIncludeTransitive` for embedding dependencies inside JARs
- **Annotation merging** — Automatically merges `META-INF/annotations/` from included JARs
- **Version management** — Centralized version properties with project-level and global overrides
- **Karaf features generation** — Generates `features.xml` from `features-src.xml` templates
- **Repository setup** — Automatically configures Maven Central, Maven Local, and Gradle Plugin Portal

## Plugin Application

In your `settings.gradle`:

```groovy
buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath 'it.water:WaterWorkspaceGradlePlugin:<version>'
    }
}

apply plugin: 'it.water.gradle.plugins.workspace'
```

## Configuration

### Modules Folder

By default, the plugin discovers projects inside a `modules` folder. To customize:

```groovy
WaterWorkspace {
    modulesFolder = 'myCustomFolder'
}
```

### Version Properties

The plugin loads versions from three sources (in order of precedence):

1. **Plugin defaults** — `versions.properties` bundled with the plugin
2. **Project-level overrides** — `water-project-custom-props.properties` in the project root
3. **Global overrides** — `water-global-custom-props.properties` in the project root

Properties are made available as Gradle `ext` properties on all projects.

## Custom Configurations

### implementationInclude

Non-transitive dependency inclusion — packages the dependency JAR directly inside your output JAR:

```groovy
dependencies {
    implementationInclude 'com.example:my-library:1.0'
}
```

### implementationIncludeTransitive

Transitive dependency inclusion — packages the dependency and all its transitive dependencies:

```groovy
dependencies {
    implementationIncludeTransitive 'com.example:my-library-with-deps:1.0'
}
```

Both configurations automatically extend `compileClasspath`, `runtimeClasspath`, `testCompileClasspath`, and `testRuntimeClasspath`.

When dependencies are included, the JAR task is configured to:
- Merge dependency JARs into the output JAR (`DuplicatesStrategy.INCLUDE`)
- Enable `zip64` for large JARs
- Merge annotation index files from `META-INF/annotations/`

## Custom Tasks

### depList

Outputs the project dependency tree as a JSON structure:

```bash
./gradlew depList
```

### generateFeatures

Generates a Karaf `features.xml` file from a `features-src.xml` template. The template can reference project dependencies using placeholders that are resolved during generation.

- **Source template:** `src/main/resources/features-src.xml`
- **Generated output:** `src/main/resources/features.xml`

```bash
./gradlew generateFeatures
```

## Build Lifecycle Hooks

The plugin hooks into Gradle's build lifecycle:

| Hook | Action |
|---|---|
| `settingsEvaluated` | Discovers and includes all projects in the workspace |
| `projectsLoaded` | Configures repositories (Maven Central, Local, Gradle Plugin Portal) |
| `projectsEvaluated` | Adds `depList` and `generateFeatures` tasks, configures JAR packaging |

## Project Discovery

The plugin walks the workspace directory tree, looking for `build.gradle` files. It excludes the following directories:

- `exam/`
- `build/`
- `target/`
- `bin/`
- `src/`

Each discovered `build.gradle` triggers automatic project inclusion into the Gradle workspace.

## Dependencies

- **Gradle API** — Settings plugin interface, build listener, task configuration
- **biz.aQute.bnd** — BND tool for OSGi bundle management (applied conditionally)
