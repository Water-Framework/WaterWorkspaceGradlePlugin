
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

package it.water.gradle.plugin.workspace;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterGradlePluginTest {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    File junitTmpWorkspaceDir;

    private Project testProject;

    private Project test2Project;


    @BeforeEach
    void setup() throws IOException {
        //copying workspace structure based in src test resources into test folder
        Path testProjectResourceDirectory = Paths.get("testProjects");
        File sourceDirectory = testProjectResourceDirectory.toFile();
        File destinationDirectory = junitTmpWorkspaceDir;
        FileUtils.copyDirectoryToDirectory(sourceDirectory, destinationDirectory);
        this.testProject = ProjectBuilder.builder().withProjectDir(new File(destinationDirectory,"testProjects/TestProject")).build();
        this.test2Project = ProjectBuilder.builder().withProjectDir(new File(destinationDirectory, "testProjects/Test2Project")).build();
    }

    @Test
    void test001_applyPluginShouldWork() throws IOException {
        BuildResult br = GradleRunner.create().withProjectDir(this.testProject.getProjectDir()).build();
        Assertions.assertNotNull(br);
    }

    @Test
    void test002_shouldStartProjectDetection() {
        BuildResult res = GradleRunner.create().withProjectDir(this.testProject.getProjectDir()).withArguments("build", "--info").build();
        System.out.println(res.getOutput());
        assertTrue(res.getOutput().contains("Settings Evaluated searching for projects..."));
        assertTrue(res.getOutput().contains("Updating global properties..."));
        assertTrue(res.getOutput().contains("Overriding props with workspace ones..."));
        assertTrue(res.getOutput().contains("Adding Repo: maven central..."));
        assertTrue(res.getOutput().contains("Adding Repo: maven local..."));
        assertTrue(res.getOutput().contains("Adding Repo: gradle m2..."));
        assertTrue(res.getOutput().contains("Adding depList task..."));
        assertTrue(res.getOutput().contains("Including :TestSubProject"));
        assertTrue(res.getOutput().contains("Including :TestSubProject:TestSubSubProject"));
    }

    @Test
    void test003_checkDepListTask() {
        BuildResult res = GradleRunner.create().withProjectDir(this.testProject.getProjectDir()).withArguments("depList", "--info").build();
        assertTrue(res.getOutput().contains("-- DEP LIST OUTPUT --"));
        assertTrue(res.getOutput().contains("-- END DEP LIST OUTPUT --"));
    }


}
