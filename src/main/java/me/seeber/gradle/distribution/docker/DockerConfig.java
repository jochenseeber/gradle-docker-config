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

import org.eclipse.jdt.annotation.Nullable;
import org.gradle.model.Managed;
import org.gradle.model.ModelMap;

/**
 * Docker configuration
 */
@Managed
public interface DockerConfig {

    /**
     * Get if we should pull images from the Docker registry
     * 
     * @return <code>true</code> if we should pull images from the Docker registry
     */
    public boolean isPull();

    /**
     * Set if we should pull images from the Docker registry
     * 
     * @param enable <code>true</code> if we should pull images from the Docker registry
     */
    public void setPull(boolean enable);

    /**
     * Get URL of docker registry
     * 
     * @return URL of docker registry
     */
    public @Nullable String getRegistryUrl();

    /**
     * Set URL of docker registry
     * 
     * @param url URL of docker registry
     */
    public void setRegistryUrl(@Nullable String url);

    /**
     * Get Docker image configurations
     * 
     * @return Docker image configurations
     */
    public ModelMap<DockerImage> getImages();

}
