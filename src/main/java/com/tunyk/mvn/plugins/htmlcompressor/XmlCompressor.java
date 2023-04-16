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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class XmlCompressor.
 */
public class XmlCompressor {

    /** The Constant FILE_EXT. */
    private static final String[] FILE_EXT = {"xml"};

    /** The file ext. */
    private String[] fileExt;
    
    /** The src dir path. */
    private String srcDirPath;
    
    /** The target dir path. */
    private String targetDirPath;
    
    /** The file encoding. */
    private Charset fileEncoding;
    
    /** The xml compressor. */
    private com.googlecode.htmlcompressor.compressor.XmlCompressor xmlCompressor;

    /**
     * Instantiates a new xml compressor.
     *
     * @param srcDirPath the src dir path
     * @param targetDirPath the target dir path
     */
    public XmlCompressor(String srcDirPath, String targetDirPath) {
        this.srcDirPath = srcDirPath;
        this.targetDirPath = targetDirPath;
    }

    /**
     * Compress.
     *
     * @throws Exception the exception
     */
    public void compress()  throws Exception {
        if (fileExt == null || fileExt.length == 0) {
            fileExt = FILE_EXT;
        }

        FileTool fileTool = new FileTool(srcDirPath, fileExt, true);
        fileTool.setFileEncoding(fileEncoding);
        Map<String, String> map = fileTool.getFiles();

        if (xmlCompressor == null) {
            xmlCompressor = new com.googlecode.htmlcompressor.compressor.XmlCompressor();
        }

        for (Entry<String, String> key : map.entrySet()) {
            map.put(key.getKey(), xmlCompressor.compress(key.getValue()));
        }

        fileTool.writeFiles(map, targetDirPath);
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
     * Gets the src dir path.
     *
     * @return the src dir path
     */
    public String getSrcDirPath() {
        return srcDirPath;
    }

    /**
     * Sets the src dir path.
     *
     * @param srcDirPath the new src dir path
     */
    public void setSrcDirPath(String srcDirPath) {
        this.srcDirPath = srcDirPath;
    }

    /**
     * Gets the target dir path.
     *
     * @return the target dir path
     */
    public String getTargetDirPath() {
        return targetDirPath;
    }

    /**
     * Sets the target dir path.
     *
     * @param targetDirPath the new target dir path
     */
    public void setTargetDirPath(String targetDirPath) {
        this.targetDirPath = targetDirPath;
    }

    /**
     * Gets the file encoding.
     *
     * @return the file encoding
     */
    public Charset getFileEncoding() {
        return fileEncoding;
    }

    /**
     * Sets the file encoding.
     *
     * @param fileEncoding the new file encoding
     */
    public void setFileEncoding(Charset fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    /**
     * Gets the xml compressor.
     *
     * @return the xml compressor
     */
    public com.googlecode.htmlcompressor.compressor.XmlCompressor getXmlCompressor() {
        return xmlCompressor;
    }

    /**
     * Sets the xml compressor.
     *
     * @param xmlCompressor the new xml compressor
     */
    public void setXmlCompressor(com.googlecode.htmlcompressor.compressor.XmlCompressor xmlCompressor) {
        this.xmlCompressor = xmlCompressor;
    }
}