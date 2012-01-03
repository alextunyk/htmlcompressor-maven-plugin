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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;

/**
 * @author Alex Tunyk <alex at tunyk.com>
 */
public class XmlCompressor {

    private static final String[] FILE_EXT = {"xml"};

    private String[] fileExt;
    private String srcDirPath;
    private String targetDirPath;
    private String fileEncoding;
    private com.googlecode.htmlcompressor.compressor.XmlCompressor xmlCompressor;

    public XmlCompressor(String srcDirPath, String targetDirPath) {
        this.srcDirPath = srcDirPath;
        this.targetDirPath = targetDirPath;
    }

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

        for(String key : map.keySet()) {
            map.put(key, xmlCompressor.compress(map.get(key)));
        }

        fileTool.writeFiles(map, targetDirPath);
    }

    public String[] getFileExt() {
        return fileExt;
    }

    public void setFileExt(String[] fileExt) {
        this.fileExt = fileExt;
    }
    
    public String getSrcDirPath() {
        return srcDirPath;
    }

    public void setSrcDirPath(String srcDirPath) {
        this.srcDirPath = srcDirPath;
    }

    public String getTargetDirPath() {
        return targetDirPath;
    }

    public void setTargetDirPath(String targetDirPath) {
        this.targetDirPath = targetDirPath;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    public com.googlecode.htmlcompressor.compressor.XmlCompressor getXmlCompressor() {
        return xmlCompressor;
    }

    public void setXmlCompressor(com.googlecode.htmlcompressor.compressor.XmlCompressor xmlCompressor) {
        this.xmlCompressor = xmlCompressor;
    }
}