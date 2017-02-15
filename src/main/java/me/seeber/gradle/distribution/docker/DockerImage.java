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

import java.util.Arrays;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.model.Managed;
import org.gradle.model.Unmanaged;

import groovy.lang.Closure;

/**
 * Docker image configuration
 */
@Managed
public abstract class DockerImage implements Named {

    /**
     * Specifies the source files or directories for the image
     *
     * @param from Source for the image
     * @param configuration Configuration added source
     *
     * @see CopySpec#from(Object, Closure)
     * @see Project#files(Object...)
     */
    @Unmanaged
    public void from(Object from, Closure<?> configuration) {
        getFiles().from(from, configuration);
    }

    /**
     * Adds the given specs as a child of this spec
     *
     * @param copySpecs Child specs to add
     */
    @Unmanaged
    public void with(CopySpec... copySpecs) {
        getFiles().with(copySpecs);
    }

    /**
     * Add dependencies to the image
     *
     * @param dependencies Dependencies to add
     */
    @Unmanaged
    public void dependsOn(Object... dependencies) {
        getDependsOn().addAll(Arrays.asList(dependencies));
    }

    /**
     * Get the repository of the image
     *
     * @return Repository of the image
     */
    public abstract @Nullable String getRepository();

    /**
     * Set the repository of the image
     *
     * @param repository Repository of the image
     */
    public abstract void setRepository(@Nullable String repository);

    /**
     * Get the tag of the image
     *
     * @return Tag of the image
     */
    public abstract @Nullable String getTag();

    /**
     * Set the tag of the image
     *
     * @param tag Tag of the image
     */
    public abstract void setTag(@Nullable String tag);

    /**
     * Get the image's dependencies
     * 
     * These dependencies are added to the task that copies the source files for the images
     * 
     * @return Dependencies
     */
    @Unmanaged
    public abstract Set<Object> getDependsOn();

    /**
     * Set the image's dependencies
     * 
     * @param dependsOn Dependencies
     */
    public abstract void setDependsOn(Set<Object> dependsOn);

    /**
     * Get the image source
     * 
     * @return Image source
     */
    @Unmanaged
    public abstract CopySpec getFiles();

    /**
     * Set the image source
     * 
     * @param copySpec Image source
     */
    public abstract void setFiles(CopySpec copySpec);

}
