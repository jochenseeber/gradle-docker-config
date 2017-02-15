/**
 * BSD 2-Clause License
 *
 * Copyright (c) 2016, Jochen Seeber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.seeber.gradle.distribution.docker;

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.gradle.api.Task;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.Copy;
import org.gradle.model.Defaults;
import org.gradle.model.Each;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;
import org.gradle.model.internal.core.Hidden;

import com.bmuschko.gradle.docker.DockerExtension;
import com.bmuschko.gradle.docker.DockerRegistryCredentials;
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

import me.seeber.gradle.plugin.AbstractProjectConfigPlugin;
import me.seeber.gradle.project.base.ProjectConfigPlugin;
import me.seeber.gradle.project.base.ProjectContext;
import me.seeber.gradle.util.Validate;

/**
 * Configure project for Docker distribution
 */
public class DockerConfigPlugin extends AbstractProjectConfigPlugin {

    /**
     * Converter to convert image names to task names
     */
    protected static final Converter<String, String> IMAGE_TO_TASK_NAME_CONVERTER = Validate
            .notNull(CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL));

    /**
     * Plugin rules
     */
    public static class PluginRules extends RuleSource {

        /**
         * Logger if we feel talkative...
         */
        private static final Logger LOGGER = Logging.getLogger(DockerConfigPlugin.class);

        /**
         * Provide the Docker configuration
         *
         * @param dockerConfig Docker configuration
         */
        @Model
        public void dockerConfig(DockerConfig dockerConfig) {
        }

        /**
         * Initialize a Docker image
         *
         * @param image Docker image to initialize
         * @param project Project context
         * @param files File operations
         */
        @Defaults
        public void initializeImage(@Each DockerImage image, ProjectContext project, FileOperations files) {
            image.setDependsOn(new HashSet<>());
            image.setFiles(files.copySpec());
            image.setRepository(project.getName());
            image.setTag(project.getVersion());
        }

        /**
         * Provide the Docker plugin's configuration
         *
         * @param extensions Extension container to fetch configuration
         * @return Docker plugin's configuration
         */
        @Model
        @Hidden
        public DockerExtension dockerExtension(ExtensionContainer extensions) {
            return extensions.getByType(DockerExtension.class);
        }

        /**
         * Initialize the docker plugin
         *
         * <ul>
         * <li>Set Docker URL using environment variable DOCKER_HOST
         * <li>Set Certificate path using environment variable DOCKER_CERT_PATH
         * <li>Set registry credentials from Gradle properties file or environment
         * </ul>
         *
         * @param docker Docker plugin configuration
         * @param project Project context
         * @param dockerConfig Docker configuration
         */
        @Defaults
        public void initializeDockerExtension(DockerExtension docker, ProjectContext project,
                DockerConfig dockerConfig) {
            DockerRegistryCredentials registryCredentials = Optional.ofNullable(docker.getRegistryCredentials())
                    .orElseGet(() -> {
                        DockerRegistryCredentials credentials = new DockerRegistryCredentials();
                        docker.setRegistryCredentials(credentials);
                        return credentials;
                    });

            Optional.ofNullable(System.getenv("DOCKER_HOST")).ifPresent(host -> {
                docker.setUrl(host.replaceFirst("tcp://", "https://"));
            });

            Optional.ofNullable(System.getenv("DOCKER_CERT_PATH")).ifPresent(path -> {
                docker.setCertPath(new File(path));
            });

            LOGGER.debug("Using Docker URL '{}'", docker.getUrl());
            LOGGER.debug("Using Docker certificate path '{}'", docker.getCertPath());

            registryCredentials.setUsername(project.getProperty("docker.user"));
            registryCredentials.setPassword(project.getProperty("docker.password"));
            registryCredentials.setEmail(project.getProperty("docker.email"));
            registryCredentials.setUrl(project.getProperty("docker.url"));
        }

        /**
         * Create docker image tasks
         *
         * Performs the following for each configured Docker image:
         * <ul>
         * <li>Create a task to copy the image files
         * <li>Create a task to build the image
         * <li>Create a task to push the image
         * </ul>
         *
         * @param tasks Task container to create tasks
         * @param dockerConfig Docker configuration
         * @param dockerExtension Docker extension (do not remove, needs to be initialized)
         * @param project Current project context
         * @param buildDir Build directory
         */
        @Mutate
        public void createTasks(ModelMap<Task> tasks, DockerConfig dockerConfig, DockerExtension dockerExtension,
                ProjectContext project, @Path("buildDir") File buildDir) {
            Map<String, Object> context = new HashMap<>();
            context.put("project", project);

            for (DockerImage image : dockerConfig.getImages()) {
                Object name = IMAGE_TO_TASK_NAME_CONVERTER.convert(image.getName());

                tasks.create(format("docker%sCopy", name), Copy.class, t -> {
                    t.setDescription(format("Copy files for Docker image '%s'", image.getName()));
                    t.setGroup("docker");
                    t.into(buildDir.toPath().resolve(Paths.get("docker", image.getName())).toFile());
                    t.from(Paths.get("src", "docker", image.getName()).toString(), c -> {
                        c.expand(context);
                    });
                    t.with(image.getFiles());
                    t.dependsOn(image.getDependsOn());
                });

                tasks.create(format("docker%sBuild", name), DockerBuildImage.class, t -> {
                    t.setDescription(format("Build Docker image '%s'", image.getName()));
                    t.setGroup("docker");
                    t.setTag(image.getRepository() + ":" + image.getTag());
                    t.setInputDir(buildDir.toPath().resolve(Paths.get("docker", image.getName())).toFile());
                    t.setPull(dockerConfig.isPull());
                    t.dependsOn(format("docker%sCopy", name));
                });

                tasks.create(format("docker%sPush", name), DockerPushImage.class, t -> {
                    t.setDescription(format("Push Docker image '%s'", image.getName()));
                    t.setGroup("docker");
                    t.setImageName(image.getRepository());
                    t.setTag(image.getTag());
                    t.dependsOn(format("docker%sBuild", name));
                });
            }
        }
    }

    /**
     * <ul>
     * <li>Apply the Project Configuration Plugin
     * <li>Apply the Docker Plugin
     * </ul>
     *
     * @see me.seeber.gradle.plugin.AbstractProjectConfigPlugin#initialize()
     */
    @Override
    public void initialize() {
        getProject().getPluginManager().apply(ProjectConfigPlugin.class);
        getProject().getPluginManager().apply(DockerRemoteApiPlugin.class);
    }
}
