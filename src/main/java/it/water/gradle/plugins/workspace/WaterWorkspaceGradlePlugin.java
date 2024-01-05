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
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
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
    //reduntant in some method but it is needed in others
    private Settings settings;

    /**
     * Step 1.
     * Adding extension to workspace and setup build listener
     *
     * @param settings
     */
    @Override
    public void apply(Settings settings) {
        this.settings = settings;
        this.extension = addWorkspaceExtension(settings);
        settings.getGradle().addBuildListener(this);
    }

    /**
     * Step 2
     * Adding projects to current build base on system property.
     *
     * @param settings
     */
    @Override
    public void settingsEvaluated(Settings settings) {
        log.info("Settings Evaluated searching for projects...");
        String projectPath = settings.getRootProject().getProjectDir().getPath();
        addProjectsToWorkspace(projectPath);

    }

    /**
     * Step 3
     * Adding repositories to ROOT project along with BND TOOLS DEP and Karaf Feature DEP
     *
     * @param gradle
     */
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
    }

    /**
     * Step 4
     * Adding default task to the root project:
     * - DepList task, used to identify cycles inside workspace dependencies amd to calculate modularity measures
     *
     * @param gradle
     */
    @Override
    public void projectsEvaluated(Gradle gradle) {
        Project project = gradle.getRootProject();
        addDepListTask(project);
        addKarafFeaturesTask(project, project);
    }

    @Override
    public void buildFinished(BuildResult buildResult) {
        //do nothing
    }

    /**
     * @param dependecies
     */
    private void addBndGradleDep(DependencyHandler dependecies) {
        log.info("Adding dependency: bndTools...");
        String bndToolsVersion = versionsProperties.getProperty("bndToolVersion");
        dependecies.add("classpath", BND_TOOL_DEP_NAME + ":" + bndToolsVersion);
    }

    /**
     * @param settings
     * @return
     */
    private WaterWorskpaceExtension addWorkspaceExtension(Settings settings) {
        ExtensionAware extensionAware = (ExtensionAware) settings.getGradle();
        ExtensionContainer extensionContainer = extensionAware.getExtensions();
        return extensionContainer.create(WATER_WS_EXTENSION, WaterWorskpaceExtension.class, settings);
    }

    /**
     * @param modulesPath
     */
    private void addProjectsToWorkspace(String modulesPath) {

        try {
            List<String> projectsFound = new ArrayList<>();
            Files.walkFileTree(Paths.get(modulesPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    File file = path.toFile();
                    if (path.toString().matches(EXCLUDE_PROJECT_PATHS)) {
                        return FileVisitResult.SKIP_SIBLINGS;
                    }else if (file.isFile() && file.getName().endsWith("build.gradle") && !file.getParent().equalsIgnoreCase(modulesPath)) {
                        //adding project only for children projects not the one containing settings.xml which is automatically added by gradle
                        projectsFound.add(file.getPath());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            projectsFound.forEach(filePath -> {
                filePath = filePath.replace(File.separator + "build.gradle", "");
                String modulesRelativePath = transformInGradlePath(filePath.substring(filePath.indexOf(modulesPath) + modulesPath.length()));
                String module = modulesRelativePath;
                includeProjectIntoWorkspace(settings, module);
            });

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param path
     * @return
     */
    private String transformInGradlePath(String path) {
        return path.replace("\\\\", ":").replace("/", ":");
    }

    /**
     * @param projectModulePath
     */
    private void includeProjectIntoWorkspace(Settings settings, String projectModulePath) {
        log.info("Including {}", projectModulePath);
        settings.include(projectModulePath);
    }

    /**
     * Adding task for checking dependencies list
     *
     * @param rootProject
     */
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
            // define limits for output in order to be parse correctly
            rootProject.getLogger().lifecycle("-- DEP LIST OUTPUT --");
            rootProject.getLogger().lifecycle(jsonStr);
            rootProject.getLogger().lifecycle("-- END DEP LIST OUTPUT --");
        }));
    }

    /**
     * @param project
     */
    private void addKarafFeaturesTask(Project rootProject, Project project) {
        File featuresSrcFile = new File(project.getProjectDir() + File.separator + FEATURES_SRC_FILE_PATH);
        //adding task to current project
        if (featuresSrcFile.exists() && !project.hasProperty("generateFeatures")) {
            addTaskToFeaturesProject(project);
        }
        //search for children projects
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
        //adding gradle plugin defined version
        log.info("Updating global properties...");
        WaterGradleWorkspaceUtil.getAllDefinedVersions().stream().forEach(propertyName -> {
            log.info("Setting global Property : {}  =  {}", propertyName, WaterGradleWorkspaceUtil.getProperty(propertyName));
            project.getExtensions().getExtraProperties().set(propertyName, WaterGradleWorkspaceUtil.getProperty(propertyName));
        });
        log.info("Overriding props with workspace ones...");
        //overriding props with those specified inside the workspace
        WaterGradleWorkspaceUtil.getWorkspaceDefinedVersions(project).stream().forEach(propertyName -> {
            log.info("Setting Custom Workspace Property : {} = {}", propertyName, WaterGradleWorkspaceUtil.getWorkspaceDefinedProperty(project, propertyName));
            project.getExtensions().getExtraProperties().set(propertyName, WaterGradleWorkspaceUtil.getWorkspaceDefinedProperty(project, propertyName));
        });
    }
}
