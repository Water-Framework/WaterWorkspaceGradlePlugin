# WaterWorkspaceGradlePlugin

## Overview

The WaterWorkspaceGradlePlugin is a Gradle plugin designed to streamline the management of multi-project Gradle workspaces, particularly within the Water framework. It simplifies the process of including projects in the workspace, managing dependencies between them, and generating Karaf features files for deployment in OSGi environments. This plugin is intended for developers and architects working on large, modular applications built with Gradle and deployed on platforms like Apache Karaf. It automates much of the boilerplate configuration required for multi-project setups, making development and maintenance more efficient.

The primary goal of this repository is to provide a reusable Gradle plugin that can be easily integrated into any Water-based project. Each internal module within the plugin contributes to this goal by providing specific functionalities:

-   **WaterWorkspaceGradlePlugin:** The core plugin class that applies the plugin logic, configures the workspace, and sets up tasks.
-   **WaterWorkspaceExtension:** A Gradle extension (currently a placeholder) intended to provide configuration options for the plugin.
-   **WaterGradleWorkspaceUtil:** A utility class that handles common tasks such as loading properties files and retrieving version information.
-   **versions.properties:** A properties file that defines the versions of various dependencies used throughout the workspace, ensuring consistency and simplifying dependency management.

## Technology Stack

*   **Language:** Java
*   **Build Tool:** Gradle
*   **Testing Framework:** JUnit Jupiter
*   **Dependency Management:** Gradle Dependency Management
*   **Code Coverage:** Jacoco
*   **Repositories:** Maven Central, Maven Local, ACSoftwareRepository (`https://nexus.acsoftware.it/nexus/repository/maven-water/`)
*   **APIs:**
    *   `java.nio.file` (for file system interaction)
    *   `java.util.Properties` (for handling properties files)
    *   `org.gradle` API

## Directory Structure

```
├── build.gradle                      - Main build file for the plugin
├── settings.gradle                   - Settings file for the plugin
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── it/water/gradle/plugins/workspace/
│   │   │       ├── WaterWorkspaceGradlePlugin.java   - Main plugin class
│   │   │       ├── WaterWorkspaceExtension.java      - Gradle extension class
│   │   │       └── WaterGradleWorkspaceUtil.java     - Utility class for workspace operations
│   │   └── resources/
│   │       └── versions.properties             - Properties file defining dependency versions
│   └── test/
│       └── java/
│           └── it/water/gradle/plugin/workspace/
│               └── WaterGradlePluginTest.java        - Test class for the plugin
├── testProjects/                     - Contains sample projects to test the plugin
│   ├── TestProject/                - A test project
│   │   ├── build.gradle              - Build file for the test project
│   │   ├── settings.gradle           - Settings file for the test project
│   │   ├── versions.properties       - Properties file for the test project
│   │   └── TestSubProject/           - A subproject within the test project
│   │       └── build.gradle          - Build file for the subproject
│   └── Test2Project/               - Another test project
│       ├── build.gradle              - Build file for the test project
│       ├── settings.gradle           - Settings file for the test project
│       ├── versions.properties       - Properties file for the test project
│       └── Test2SubProject/          - A subproject within the test project
│           └── build.gradle          - Build file for the subproject
└── README.md                         - Project documentation
```

## Getting Started

1.  **Prerequisites:**
    *   Java Development Kit (JDK) version 11 or higher
    *   Gradle version 6.0 or higher

2.  **Clone the repository:**

    ```bash
    git clone https://github.com/Water-Framework/WaterWorkspaceGradlePlugin.git
    ```

3.  **Build the plugin:**

    ```bash
    cd WaterWorkspaceGradlePlugin
    gradle build
    ```

    This command compiles the code, runs the tests, and packages the plugin into a JAR file located in the `build/libs` directory.

4.  **Publish the plugin locally (optional):**

    ```bash
    gradle publishToMavenLocal
    ```

    This command publishes the plugin to your local Maven repository, allowing you to use it in other projects on your machine.

5.  **Using the plugin in your project:**

    *   Add the following to your project's `settings.gradle` file:

        ```gradle
        buildscript {
            repositories {
                mavenLocal()
                maven {
                    name = 'ACSoftwareRepository'
                    url = "https://nexus.acsoftware.it/nexus/repository/maven-water/"
                }
            }
            dependencies {
                classpath 'it.water.gradle.plugins.workspace:WaterWorkspaceGradlePlugin:3.0.0'
            }
        }

        apply plugin: 'it.water.workspace'
        ```

    *   Ensure that the `mavenLocal()` and `ACSoftwareRepository` repositories are configured to resolve the plugin dependency.

### Module Usage

*   **WaterWorkspaceGradlePlugin:** This is the main entry point for the plugin. Applying the plugin to your `settings.gradle` file automatically configures the workspace to include all subprojects. It scans the project directory structure for `build.gradle` files and includes the corresponding projects in the workspace. The plugin also adds tasks for listing dependencies and generating Karaf features files.

    *   **Example:** To enable the plugin, simply add `apply plugin: 'it.water.workspace'` to your `settings.gradle` file.  The plugin will automatically detect subprojects and include them in the workspace.  Ensure the `buildscript` block in `settings.gradle` correctly declares the plugin's classpath.
    *   **Configuration:** The plugin uses convention over configuration.  Projects are automatically included based on directory structure. Future versions will provide more explicit configuration options via the `WaterWorkspaceExtension`.

*   **WaterWorkspaceExtension:** This Gradle extension is intended to provide configuration options for the plugin. Currently, it is a placeholder class, but future versions will allow you to customize the plugin's behavior, such as excluding specific projects from the workspace or configuring the Karaf features file generation.

    *   **Example (Future):** To exclude a project, you might configure the extension like this:

        ```gradle
        waterWorkspace {
            excludeProjects = ['project-to-exclude']
        }
        ```

        This functionality is not yet implemented but illustrates the intended usage of the extension.

*   **WaterGradleWorkspaceUtil:** This utility class provides helper methods for loading properties files and retrieving version information. It is used internally by the plugin to manage dependencies and ensure consistency across the workspace. You can also use it in your own build scripts to access version information defined in the `versions.properties` file.

    *   **Example:** To access a version property in your `build.gradle` file:

        ```gradle
        def props = it.water.gradle.plugins.workspace.util.WaterGradleWorkspaceUtil.loadPropertiesFile('versions.properties', project)
        def waterVersion = props.getProperty('waterVersion')
        println "Water Version: $waterVersion"
        ```

        This code snippet demonstrates how to load the `versions.properties` file and retrieve the value of the `waterVersion` property.

## Functional Analysis

### 1. Main Responsibilities of the System

The primary responsibility of the `WaterWorkspaceGradlePlugin` is to automate the configuration and management of multi-project Gradle workspaces, especially those within the Water framework. It handles project discovery, dependency management, and Karaf features file generation. The plugin acts as an orchestrator, bringing together different aspects of workspace management into a single, cohesive solution. It also provides foundational services for accessing version information and managing dependencies consistently.

### 2. Problems the System Solves

The plugin addresses several key challenges in multi-project development:

*   **Boilerplate Configuration:** Manually configuring a large multi-project workspace can be tedious and error-prone. The plugin automates this process, reducing the amount of manual configuration required.
*   **Dependency Management:** Ensuring consistent versions of dependencies across multiple projects can be difficult. The plugin provides mechanisms for managing versions centrally, ensuring consistency and simplifying upgrades.
*   **Karaf Deployment:** Deploying modular applications to Karaf requires the creation of features files. The plugin automates the generation of these files, making deployment easier.
*   **Project Discovery:** Automatically detecting and including new projects in the workspace simplifies the process of adding new modules to the application.

### 3. Interaction of Modules and Components

The `WaterWorkspaceGradlePlugin` interacts with other Gradle components through the Gradle API. It uses the `Settings` object to configure the workspace, the `Project` object to manage dependencies and tasks, and the `DependencyHandler` to add dependencies. The `WaterGradleWorkspaceUtil` class is used to load properties files and retrieve version information. The plugin follows a convention-over-configuration approach, automatically detecting and including projects in the workspace based on the presence of `build.gradle` files.

The plugin's components interact as follows:

1.  The `apply()` method in `WaterWorkspaceGradlePlugin` is invoked when the plugin is applied to the `settings.gradle` file.
2.  The `apply()` method adds the `waterWorkspace` extension and sets up listeners for project loading and evaluation.
3.  The `projectsLoaded()` method is invoked after project loading and is responsible for adding projects to the workspace.
4.  The `WaterGradleWorkspaceUtil` class is used to load properties from `versions.properties` and make them available to the build.
5.  The plugin adds tasks for listing dependencies and generating Karaf features files.

### 4. User-Facing vs. System-Facing Functionalities

The `WaterWorkspaceGradlePlugin` primarily provides system-facing functionalities. It automates tasks and configurations that are typically performed by developers and build engineers. The plugin does not have a direct user interface, but its functionalities are exposed through Gradle tasks and configuration options.

*   **System-Facing Functionalities:**
    *   Automated project inclusion in the workspace.
    *   Dependency management and version control.
    *   Karaf features file generation.
    *   Task for listing dependencies.

The plugin enhances the developer experience by simplifying workspace management and reducing the amount of manual configuration required.

### Common Annotations and Behaviors

While there isn't a single interface or abstract class applying common annotations systematically, the plugin leverages Gradle's conventions and dependency injection to ensure consistent behavior across different parts of the build. For instance, dependency versions are centrally managed in `versions.properties` and accessed via `WaterGradleWorkspaceUtil`, promoting consistency.

## Architectural Patterns and Design Principles Applied

*   **Plugin Pattern:** The project follows the Gradle plugin pattern, encapsulating workspace management logic within a reusable plugin. This allows developers to easily integrate the plugin into their projects by simply applying it in the `settings.gradle` file.
*   **Extension Pattern:** The `waterWorkspace` extension (currently a placeholder) is intended to allow users to configure the plugin's behavior. This provides a flexible way to customize the plugin to meet the specific needs of different projects.
*   **Utility Class:** The `WaterGradleWorkspaceUtil` class provides utility functions for common tasks such as loading properties files and retrieving version information. This promotes code reuse and reduces duplication.
*   **Convention over Configuration:** The plugin uses convention over configuration, automatically detecting and including projects in the workspace based on directory structure. This simplifies the configuration process and reduces the amount of manual configuration required.
*   **Modularity:** The plugin promotes modularity by facilitating the creation of Karaf features files. This allows developers to deploy modular applications to Karaf.
*   **Dependency Injection:** The plugin uses Gradle's dependency injection mechanism to manage dependencies. This simplifies dependency management and promotes loose coupling.


## Weaknesses and Areas for Improvement

The following items represent areas for improvement and future development:

*   [ ] **Implement static analysis:** Integrate tools like SonarQube or FindBugs to automatically detect bugs, vulnerabilities, and code smells.
*   [ ] **Generate code coverage reports:** Configure Jacoco to generate code coverage reports and identify areas with low test coverage.
*   [ ] **Refactor duplicated code:** Identify and refactor duplicated code blocks in `build.gradle` files into reusable functions or Gradle conventions.
*   [ ] **Establish coding standards:** Define coding standards and conduct code reviews to ensure code quality and consistency.
*   [ ] **Improve security:** Implement input validation to prevent potential vulnerabilities related to file paths and user-provided data. Secure credential handling for Maven publishing, potentially using Gradle's built-in credential storage.
*   [ ] **Enhance logging:** Replace `println` statements with Gradle's logging system for better control and context.
*   [ ] **Implement WaterWorkspaceExtension:** Implement configurable properties in the `WaterWorkspaceExtension` class to allow users to customize the plugin's behavior, such as excluding specific projects from the workspace or configuring the Karaf features file generation.
*   [ ] **Run code coverage regularly:** Integrate code coverage report generation into the regular build process.
*   [ ] **Improve error handling:** Implement more robust error handling, especially when loading properties files or interacting with the file system.
*   [ ] **Provide explicit configuration options:** Add explicit configuration options to the `WaterWorkspaceExtension` to manage project inclusion and exclusion more effectively.
*   [ ] **Review and improve tests:** Review and improve the tests in `WaterGradlePluginTest` to ensure comprehensive testing of the plugin's functionality.
*   [ ] **Address Placeholder Extension:** Fully implement the `WaterWorkspaceExtension` to allow customization of plugin behavior. This is a critical functional gap.
*   [ ] **Clarify Project Inclusion/Exclusion:** Provide clearer documentation and configuration options for explicitly including or excluding projects from the workspace.
*   [ ] **Enhance Dependency Management:** Investigate more sophisticated dependency management strategies, such as dependency locking or version alignment, to ensure consistency across the workspace.
*   [ ] **Improve Test Coverage:** Focus on increasing test coverage for the core plugin logic, especially the project discovery and Karaf features file generation functionalities.
*   [ ] **Address Potential Code Duplication:** Refactor common build configurations into reusable functions or Gradle conventions to reduce redundancy.

## Further Areas of Investigation

The following architectural and technical elements warrant further investigation:

*   **Performance Bottlenecks:** Analyze the plugin's performance when working with very large workspaces (hundreds or thousands of projects) to identify potential bottlenecks.
*   **Scalability Considerations:** Evaluate the plugin's scalability and identify potential limitations in terms of the number of projects it can handle.
*   **Integration with External Systems:** Explore potential integrations with external systems such as CI/CD pipelines or artifact repositories.
*   **Advanced Features:** Research and implement advanced features such as dependency conflict resolution or automatic version alignment.
*   **Code Smells and Low Test Coverage:** Conduct a thorough code review to identify and address code smells and areas with low test coverage.
*   **Dynamic Project Discovery:** Explore options for more dynamic project discovery, such as automatically detecting new projects added to the workspace.
*   **Karaf Features Customization:** Provide more options for customizing the generated Karaf features files, such as specifying different feature repositories or adding custom feature configurations.

## Attribution

Generated with the support of ArchAI, an automated documentation system.
