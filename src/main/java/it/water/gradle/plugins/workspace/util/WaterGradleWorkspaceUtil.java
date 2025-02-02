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

package it.water.gradle.plugins.workspace.util;

import org.gradle.api.Project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;
import java.util.Set;

public class WaterGradleWorkspaceUtil {

    private static Properties versions;


    static {
        //load locally from the gradle plugin
        versions = new Properties();
        try {
            versions.load(WaterGradleWorkspaceUtil.class.getClassLoader().getResourceAsStream("versions.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WaterGradleWorkspaceUtil() {
    }

    public static Set<String> getAllDefinedVersions() {
        return versions.stringPropertyNames();
    }

    public static Set<String> getWorkspaceDefinedVersions(Project rootProject) {
        return loadWorkspaceProperties(rootProject).stringPropertyNames();
    }

    public static String getWorkspaceDefinedProperty(Project rootProject, String name) {
        return loadWorkspaceProperties(rootProject).getProperty(name);
    }

    private static Properties loadWorkspaceProperties(Project rootProject) {
        //loaded from the current worskspace
        String rootProjectPath = rootProject.getProjectDir().getAbsolutePath();
        String projectCustomProperties = rootProjectPath + File.separator + "water-project-custom-props.properties";
        String globalCustomProperties = rootProjectPath + File.separator + ".." + File.separator + "water-global-custom-props.properties";
        Properties projectCustomProps = loadPropertiesFile(projectCustomProperties, rootProject);
        Properties globalCustomProps = loadPropertiesFile(globalCustomProperties, rootProject);
        Properties properties = new Properties();
        properties.putAll(projectCustomProps);
        properties.putAll(globalCustomProps);
        return properties;
    }

    private static Properties loadPropertiesFile(String propertiesFilePath, Project rootProject) {
        Properties workspaceDefinedVersions = new Properties();
        try (FileReader fr = new FileReader(propertiesFilePath)) {
            workspaceDefinedVersions.load(fr);
        } catch (FileNotFoundException e) {
            rootProject.getLogger().info(propertiesFilePath+" not found inside the workspace.");
        } catch (Exception t) {
            t.printStackTrace();
        }
        return workspaceDefinedVersions;
    }

    public static String getProperty(String name) {
        return versions.getProperty(name);
    }
}
