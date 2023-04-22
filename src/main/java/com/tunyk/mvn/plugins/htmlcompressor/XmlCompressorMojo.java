/*
 * Copyright (c) 2011-2023 Alex Tunyk <alex at tunyk.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 */
package com.tunyk.mvn.plugins.htmlcompressor;

import java.nio.charset.Charset;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Compress XML files.
 */
@Mojo(name = "xml", defaultPhase = LifecyclePhase.COMPILE, requiresProject = false, threadSafe = true)
public class XmlCompressorMojo extends AbstractMojo {

    /** file types to be processed. */
    @Parameter(property="htmlcompressor.fileExt")
    private String[] fileExt;

    /** if false all compression is off (default is true). */
    @Parameter(property="htmlcompressor.enabled", defaultValue="true")
    private boolean enabled = true;

    /** Skip run of plugin. */
    @Parameter(defaultValue = "false", alias = "skip", property = "skip")
    private boolean skip;

    /** if false keeps XML comments (default is true). */
    @Parameter(property="htmlcompressor.removeComments", defaultValue="true")
    private boolean removeComments = true;

    /** removes iter-tag whitespace characters  (default is true). */
    @Parameter(property="htmlcompressor.removeIntertagSpaces", defaultValue="true")
    private boolean removeIntertagSpaces = true;

    /**
     * source folder where xml files are located.
     */
    @Parameter(property="htmlcompressor.srcFolder", defaultValue="${project.basedir}/src/main/resources")
    private String srcFolder = "src/main/resources";

    /**
     * target folder where compressed xml files will be placed.
     */
    @Parameter(property="htmlcompressor.targetFolder", defaultValue="${project.build.directory}/classes")
    private String targetFolder = "target/classes";

    /** Charset encoding for files to read and create. */
    @Parameter(property="htmlcompressor.encoding", defaultValue="UTF-8")
    private String encoding = "UTF-8";

    @Override
    public void execute() throws MojoExecutionException {
        // Check if plugin run should be skipped
        if (this.skip) {
            getLog().info("XMLCompressor is skipped");
            return;
        }

        if (!enabled) {
            getLog().info("XML compression was turned off.");
            return;
        }

        getLog().info("Compressing " + srcFolder);
        XmlCompressor xmlCompressor = new XmlCompressor(srcFolder, targetFolder);
        xmlCompressor.setFileExt(fileExt);
        xmlCompressor.setFileEncoding(Charset.forName(encoding));

        com.googlecode.htmlcompressor.compressor.XmlCompressor xmlCompressorHandler = new com.googlecode.htmlcompressor.compressor.XmlCompressor();
        xmlCompressorHandler.setEnabled(enabled);
        xmlCompressorHandler.setRemoveComments(removeComments);
        xmlCompressorHandler.setRemoveIntertagSpaces(removeIntertagSpaces);
        xmlCompressor.setXmlCompressor(xmlCompressorHandler);

        try {
            xmlCompressor.compress();
        } catch(Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }

        getLog().info("XML compression completed.");
    }

    /**
     * Gets the file ext.
     *
     * @return the file ext
     */
    public String[] getFileExt() {
        return fileExt;
    }

    /**
     * Sets the file ext.
     *
     * @param fileExt the new file ext
     */
    public void setFileExt(String[] fileExt) {
        this.fileExt = fileExt;
    }
    
    /**
     * Gets the enabled.
     *
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the new enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the removes the comments.
     *
     * @return the removes the comments
     */
    public Boolean getRemoveComments() {
        return removeComments;
    }

    /**
     * Sets the removes the comments.
     *
     * @param removeComments the new removes the comments
     */
    public void setRemoveComments(Boolean removeComments) {
        this.removeComments = removeComments;
    }

    /**
     * Gets the removes the intertag spaces.
     *
     * @return the removes the intertag spaces
     */
    public Boolean getRemoveIntertagSpaces() {
        return removeIntertagSpaces;
    }

    /**
     * Sets the removes the intertag spaces.
     *
     * @param removeIntertagSpaces the new removes the intertag spaces
     */
    public void setRemoveIntertagSpaces(Boolean removeIntertagSpaces) {
        this.removeIntertagSpaces = removeIntertagSpaces;
    }

    /**
     * Gets the src folder.
     *
     * @return the src folder
     */
    public String getSrcFolder() {
        return srcFolder;
    }

    /**
     * Sets the src folder.
     *
     * @param srcFolder the new src folder
     */
    public void setSrcFolder(String srcFolder) {
        this.srcFolder = srcFolder;
    }

    /**
     * Gets the target folder.
     *
     * @return the target folder
     */
    public String getTargetFolder() {
        return targetFolder;
    }

    /**
     * Sets the target folder.
     *
     * @param targetFolder the new target folder
     */
    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }
}
