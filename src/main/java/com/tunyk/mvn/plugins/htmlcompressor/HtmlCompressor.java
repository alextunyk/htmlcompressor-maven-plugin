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
public class HtmlCompressor {

    private static final String[] FILE_EXT = {"htm", "html"};

    private String[] fileExt;
    private String srcDirPath;
    private String targetDirPath;
    private String fileEncoding;
    private boolean createJsonFile;
    private String targetJsonFilePath;
    private String jsonIntegrationFilePath;
    private com.googlecode.htmlcompressor.compressor.HtmlCompressor htmlCompressor;

    public HtmlCompressor(String srcDirPath, String targetDirPath) {
        this.srcDirPath = srcDirPath;
        this.targetDirPath = targetDirPath;
    }

    public HtmlCompressor(String srcDirPath, String targetDirPath, boolean createJsonFile, String targetJsonFilePath, String jsonIntegrationFilePath) {
        this.srcDirPath = srcDirPath;
        this.targetDirPath = targetDirPath;
        this.createJsonFile = createJsonFile;
        this.targetJsonFilePath = targetJsonFilePath;
        this.jsonIntegrationFilePath = jsonIntegrationFilePath;
    }

    public void compress()  throws Exception {
        if (fileExt == null || fileExt.length == 0) {
            fileExt = FILE_EXT;
        }
        
        FileTool fileTool = new FileTool(srcDirPath, fileExt, true);
        fileTool.setFileEncoding(fileEncoding);
        Map<String, String> map = fileTool.getFiles();

        if (htmlCompressor == null) {
            htmlCompressor = new com.googlecode.htmlcompressor.compressor.HtmlCompressor();
        }

        for(String key : map.keySet()) {
            map.put(key, htmlCompressor.compress(map.get(key)));
        }

        fileTool.writeFiles(map, targetDirPath);
        if (createJsonFile) {
            String jsonIntegrationCode = FileUtils.readFileToString(new File(jsonIntegrationFilePath));
            fileTool.writeToJsonFile(map, targetJsonFilePath, jsonIntegrationCode);
        }
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

    public boolean isCreateJsonFile() {
        return createJsonFile;
    }

    public void setCreateJsonFile(boolean createJsonFile) {
        this.createJsonFile = createJsonFile;
    }

    public String getTargetJsonFilePath() {
        return targetJsonFilePath;
    }

    public void setTargetJsonFilePath(String targetJsonFilePath) {
        this.targetJsonFilePath = targetJsonFilePath;
    }

    public String getJsonIntegrationFilePath() {
        return jsonIntegrationFilePath;
    }

    public void setJsonIntegrationFilePath(String jsonIntegrationFilePath) {
        this.jsonIntegrationFilePath = jsonIntegrationFilePath;
    }

    public com.googlecode.htmlcompressor.compressor.HtmlCompressor getHtmlCompressor() {
        return htmlCompressor;
    }

    public void setHtmlCompressor(com.googlecode.htmlcompressor.compressor.HtmlCompressor htmlCompressor) {
        this.htmlCompressor = htmlCompressor;
    }
}