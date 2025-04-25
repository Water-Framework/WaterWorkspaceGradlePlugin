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

package it.water.gradle.plugins.workspace;

import groovy.json.JsonOutput;
import it.water.gradle.plugins.workspace.util.WaterGradleWorkspaceUtil;
import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @Author Aristide Cittadino
 * Water Gradle Workspace Plugin
 * This plugin allows to automatically discover water projects and configure them.
 */
public class WaterWorkspaceGradlePlugin implements Plugin<Settings>, BuildListener {
    private static Logger log = LoggerFactory.getLogger(WaterWorkspaceGradlePlugin.class);
    public static final String INCLUDE_IN_JAR_CONF = "implementationInclude";
    public static final String INCLUDE_IN_JAR_TRANSITIVE_CONF = "implementationIncludeTransitive";
    public static final String WATER_WS_EXTENSION = "WaterWorkspace";
    public static final String EXCLUDE_PROJECT_PATHS;
    public static final String BND_TOOL_DEP_NAME = "biz.aQute.bnd:biz.aQute.bnd.gradle";
    public static final String FEATURES_SRC_FILE_PATH = "src/main/resources/features-src.xml";
    public static final String FEATURES_FILE_PATH = "src/main/resources/features.xml";
    private static Properties versionsProperties;

    static {
        EXCLUDE_PROJECT_PATHS = ".*/exam/.*|.*/build/.*|.*/target/.*|.*/bin/.*|.*/src/.*";
        versionsProperties = new Properties();
        try {
            versionsProperties.load(WaterWorkspaceGradlePlugin.class.getClassLoader().getResourceAsStream("versions.properties"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private WaterWorskpaceExtension extension;
    // Redundant in some methods but it is needed in others
    private Settings settings;

    @Override
    public void apply(Settings settings) {
        this.settings = settings;
        this.extension = addWorkspaceExtension(settings);
        settings.getGradle().addBuildListener(this);
    }

    @Override
    public void settingsEvaluated(Settings settings) {
        log.info("Settings Evaluated searching for projects...");
        String projectPath = settings.getRootProject().getProjectDir().getPath();
        addProjectsToWorkspace(projectPath);
    }

    @Override
    public void projectsLoaded(Gradle gradle) {
        Project project = gradle.getRootProject();
        defineDefaultProperties(project);
        log.info("Adding Repo: maven central...");
        project.getBuildscript().getRepositories().add(project.getBuildscript().getRepositories().mavenCentral());
        log.info("Adding Repo: maven local...");
        project.getBuildscript().getRepositories().add(project.getBuildscript().getRepositories().mavenLocal());
        log.info("Adding Repo: gradle m2...");
        project.getBuildscript().getRepositories().add(project.getBuildscript().getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl("https://plugins.gradle.org/m2/")));
        this.addBndGradleDep(project.getBuildscript().getDependencies());
        //adding includeInJar and includeInJarTransitive
        project.getAllprojects().forEach(childProject -> {
            if (addConfiguration(childProject, INCLUDE_IN_JAR_CONF, false))
                includeInJarConfiguration(childProject, INCLUDE_IN_JAR_CONF);
            if (addConfiguration(childProject, INCLUDE_IN_JAR_TRANSITIVE_CONF, true))
                includeInJarConfiguration(childProject, INCLUDE_IN_JAR_TRANSITIVE_CONF);
        });
    }

    @Override
    public void projectsEvaluated(Gradle gradle) {
        Project project = gradle.getRootProject();
        addDepListTask(project);
        addKarafFeaturesTask(project, project);
    }

    @Override
    public void buildFinished(BuildResult buildResult) {
        // Do nothing
    }

    private void addBndGradleDep(DependencyHandler dependencies) {
        log.info("Adding dependency: bndTools...");
        String bndToolsVersion = versionsProperties.getProperty("bndToolVersion");
        dependencies.add("classpath", BND_TOOL_DEP_NAME + ":" + bndToolsVersion);
    }

    private boolean addConfiguration(Project project, String configurationName, boolean transitive) {
        log.info("Adding custom dependency configuration: {}...", configurationName);
        if (project.getConfigurations().stream().noneMatch(configuration -> configuration.getName().equals(configurationName))) {
            project.getConfigurations().create(configurationName, configuration -> {
                configuration.setCanBeResolved(true);
                configuration.setCanBeConsumed(false);
                configuration.setTransitive(transitive);
            });
            log.info("Configuration {} added with properties: canBeResolved(true), canBeConsumed(false), transitive({}).", configurationName, transitive);
            return true;
        }
        log.info("Skipping adding {} because already defined inside the build gradle");
        return false;
    }

    private void includeInJarConfiguration(Project project, String configurationName) {
        project.getTasks().withType(org.gradle.api.tasks.bundling.Jar.class).configureEach(jar -> {
            log.info("Customizing jar task to include {} dependencies...", configurationName);
            jar.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
            jar.setZip64(true);
            jar.from(project.getConfigurations().getByName(configurationName).resolve().stream().map(file -> {
                return file.isDirectory() ? project.fileTree(file) : project.zipTree(file);
            }).toArray());
            log.info("Jar {} customization completed.", configurationName);
        });
    }

    private WaterWorskpaceExtension addWorkspaceExtension(Settings settings) {
        ExtensionAware extensionAware = (ExtensionAware) settings.getGradle();
        ExtensionContainer extensionContainer = extensionAware.getExtensions();
        return extensionContainer.create(WATER_WS_EXTENSION, WaterWorskpaceExtension.class, settings);
    }

    private void addProjectsToWorkspace(String modulesPath) {
        try {
            List<String> projectsFound = new ArrayList<>();
            Files.walkFileTree(Paths.get(modulesPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    File file = path.toFile();
                    if (path.toString().matches(EXCLUDE_PROJECT_PATHS)) {
                        return FileVisitResult.SKIP_SIBLINGS;
                    } else if (file.isFile() && file.getName().endsWith("build.gradle") && !file.getParent().equalsIgnoreCase(modulesPath)) {
                        log.info("Found build gradle project: " + file.getPath());
                        projectsFound.add(file.getPath());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            projectsFound.forEach(filePath -> {
                filePath = filePath.replace(File.separator + "build.gradle", "");
                String modulesRelativePath = transformInGradlePath(filePath.substring(filePath.indexOf(modulesPath) + modulesPath.length()));
                String module = modulesRelativePath;
                if (module.startsWith(File.separator)) module = module.substring(1);
                log.info("Before Adding project: " + module);
                includeProjectIntoWorkspace(settings, module);
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String transformInGradlePath(String path) {
        return path.replace("\\\\", ":").replace("/", ":");
    }

    private void includeProjectIntoWorkspace(Settings settings, String projectModulePath) {
        log.info("Including {}", projectModulePath);
        settings.include(projectModulePath);
    }

    private void addDepListTask(Project rootProject) {
        Map<String, HashMap<String, Object>> depJson = new HashMap<>();
        log.info("Adding depList task...");
        rootProject.task("depList", task -> task.doFirst(innerTask -> {
            Set<Project> subProjects = rootProject.getSubprojects();
            subProjects.stream().forEach(p -> {
                String projectName = p.getGroup() + ":" + p.getName() + ":" + p.getVersion();
                String parentProjectName = p.getParent().getGroup() + ":" + p.getParent().getName() + ":" + p.getParent().getVersion();
                if (depJson.get(projectName) == null) {
                    depJson.put(projectName, new HashMap<>());
                    depJson.get(projectName).put("parent", parentProjectName);
                    depJson.get(projectName).put("dependencies", new ArrayList<String>());
                    depJson.get(projectName).put("path", p.getBuildFile().getPath().replace("/build.gradle", ""));
                }
                p.getConfigurations().stream().forEach(conf -> conf.getDependencies().stream().forEach(it -> {
                    List<String> depList = (List<String>) depJson.get(projectName).get("dependencies");
                    depList.add(it.getGroup() + ":" + it.getName() + ":" + it.getVersion());
                }));
            });
            String jsonStr = groovy.json.JsonOutput.prettyPrint(JsonOutput.toJson(depJson));
            rootProject.getLogger().lifecycle("-- DEP LIST OUTPUT --");
            rootProject.getLogger().lifecycle(jsonStr);
            rootProject.getLogger().lifecycle("-- END DEP LIST OUTPUT --");
        }));
    }

    private void addKarafFeaturesTask(Project rootProject, Project project) {
        File featuresSrcFile = new File(project.getProjectDir() + File.separator + FEATURES_SRC_FILE_PATH);
        if (featuresSrcFile.exists() && !project.hasProperty("generateFeatures")) {
            addTaskToFeaturesProject(project);
        }
        if (project.getSubprojects() != null && !project.getSubprojects().isEmpty()) {
            project.getSubprojects().forEach(subproject -> addKarafFeaturesTask(rootProject, subproject));
        }
    }

    private void addTaskToFeaturesProject(Project project) {
        project.task("generateFeatures", task ->
                task.doLast(innerTask -> {
                    try {
                        String featuresSrcPath = project.getProjectDir().getAbsolutePath() + File.separator + FEATURES_SRC_FILE_PATH;
                        String featuresOutputPath = project.getProjectDir().getAbsolutePath() + File.separator + FEATURES_FILE_PATH;
                        String inputFileContent = new String(Files.readAllBytes(Paths.get(featuresSrcPath)));
                        String outputFileContent = inputFileContent;
                        Iterator<String> it = project.getProperties().keySet().iterator();
                        while (it.hasNext()) {
                            String key = it.next();
                            Object value = project.getProperties().get(key);
                            String token = "\\$\\{project." + key + "\\}";
                            if (value != null) {
                                outputFileContent = outputFileContent.replaceAll(token, value.toString());
                            }
                        }
                        Files.write(Paths.get(featuresOutputPath), outputFileContent.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
    }

    private void defineDefaultProperties(Project project) {
        log.info("Updating global properties...");
        WaterGradleWorkspaceUtil.getAllDefinedVersions().stream().forEach(propertyName -> {
            log.info("Setting global Property : {}  =  {}", propertyName, WaterGradleWorkspaceUtil.getProperty(propertyName));
            project.getExtensions().getExtraProperties().set(propertyName, WaterGradleWorkspaceUtil.getProperty(propertyName));
        });
        log.info("Overriding props with workspace ones...");
        WaterGradleWorkspaceUtil.getWorkspaceDefinedVersions(project).stream().forEach(propertyName -> {
            log.info("Setting Custom Workspace Property : {} = {}", propertyName, WaterGradleWorkspaceUtil.getWorkspaceDefinedProperty(project, propertyName));
            project.getExtensions().getExtraProperties().set(propertyName, WaterGradleWorkspaceUtil.getWorkspaceDefinedProperty(project, propertyName));
        });
    }
}