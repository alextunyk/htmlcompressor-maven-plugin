/*
 * Copyright (c) 2011 Alex Tunyk <alex at tunyk.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Compress XML files
 *
 * @goal xml
 * @author Alex Tunyk <alex at tunyk.com>
 */
public class XmlCompressorMojo extends AbstractMojo {

    /**
     * file types to be processed
     *
     * @parameter expression="${htmlcompressor.fileExt}"
     */
    private String[] fileExt;

    /**
     * if false all compression is off (default is true)
     *
     * @parameter expression="${htmlcompressor.enabled}" default-value="true"
     */
    private Boolean enabled = true;

    /**
     * if false keeps XML comments (default is true)
     *
     * @parameter expression="${htmlcompressor.removeComments}" default-value="true"
     */
    private Boolean removeComments = true;

    /**
     * removes iter-tag whitespace characters  (default is true)
     *
     * @parameter expression="${htmlcompressor.removeIntertagSpaces}" default-value="true"
     */
    private Boolean removeIntertagSpaces = true;

    /**
     * source folder where xml files are located.
     *
     * @parameter expression="${htmlcompressor.srcFolder}" default-value="${basedir}/src/main/resources/xml"
     */
    private String srcFolder = "src/main/resources/xml";

    /**
     * target folder where compressed xml files will be placed.
     *
     * @parameter expression="${htmlcompressor.targetFolder}" default-value="${project.build.directory}/htmlcompressor/xml"
     */
    private String targetFolder = "target/htmlcompressor/xml";

    /**
     * Charset encoding for files to read and create
     *
     * @parameter expression="${htmlcompressor.encoding}" default-value="utf-8"
     */
    private String encoding = "utf-8";

    public void execute() throws MojoExecutionException {
        if (!enabled) {
            getLog().info("XML compression was turned off.");
            return;
        }

        getLog().info("Compressing " + srcFolder);
        XmlCompressor xmlCompressor = new XmlCompressor(srcFolder, targetFolder);
        xmlCompressor.setFileExt(fileExt);
        xmlCompressor.setFileEncoding(encoding);

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

    public String[] getFileExt() {
        return fileExt;
    }

    public void setFileExt(String[] fileExt) {
        this.fileExt = fileExt;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getRemoveComments() {
        return removeComments;
    }

    public void setRemoveComments(Boolean removeComments) {
        this.removeComments = removeComments;
    }

    public Boolean getRemoveIntertagSpaces() {
        return removeIntertagSpaces;
    }

    public void setRemoveIntertagSpaces(Boolean removeIntertagSpaces) {
        this.removeIntertagSpaces = removeIntertagSpaces;
    }

    public String getSrcFolder() {
        return srcFolder;
    }

    public void setSrcFolder(String srcFolder) {
        this.srcFolder = srcFolder;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }
}