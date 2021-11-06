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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

public class FileTool {

    private String rootDirPath;
    private String[] fileExt;
    private boolean recursive;
    private Charset fileEncoding;

    public FileTool(String rootDir, String[] fileExt, boolean recursive) throws IOException {
        this.setRootDirPath(rootDir);
        this.fileExt = fileExt;
        this.recursive = recursive;
    }

    public Map<String, String> getFiles() throws IOException {
        Map<String, String> map = new HashMap<>();
        File rootDir = new File(rootDirPath);
        Collection<File> files = FileUtils.listFiles(rootDir, fileExt, recursive);
        int truncationIndex = 0;
        for (File file : files) {
            String normalizedFilePath = file.getCanonicalPath().replace("\\", "/");
            if (truncationIndex == 0) {
                truncationIndex = normalizedFilePath.indexOf(rootDirPath) + rootDirPath.length() + 1;
            }
            String key = normalizedFilePath.substring(truncationIndex);
            String value = FileUtils.readFileToString(file, fileEncoding);
            map.put(key, value);
        }
        return map;
    }

    public void writeFiles(Map<String, String> map, String targetDir) throws IOException {
        for (Entry<String, String> entry : map.entrySet()) {
            File file = new File(targetDir + "/" + entry.getKey());
            FileUtils.writeStringToFile(file, entry.getValue(), fileEncoding);
        }
    }

    public void writeToJsonFile(Map<String, String> map, String targetFile, String integrationCode) throws IOException, JSONException {
        String replacePattern = "%s";
        File file = new File(targetFile);
        JSONObject json = new JSONObject();
        for (Entry<String, String> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        if (integrationCode == null) {
            integrationCode = replacePattern;
        }
        if (integrationCode.indexOf(replacePattern) == -1) {
            integrationCode += replacePattern;
        }
        String contents = integrationCode.replaceFirst(replacePattern, Matcher.quoteReplacement(json.toString()));
        FileUtils.writeStringToFile(file, contents, fileEncoding);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getElapsedHMSTime(long elapsedTime) {
        String format = String.format("%%0%dd", 2);
        elapsedTime = elapsedTime / 1000;
        String seconds = String.format(format, elapsedTime % 60);
        String minutes = String.format(format, (elapsedTime % 3600) / 60);
        String hours = String.format(format, elapsedTime / 3600);
        return hours + ":" + minutes + ":" + seconds;
    }

    public String getRootDirPath() {
        return rootDirPath;
    }

    public void setRootDirPath(String rootDirPath) throws IOException {
        File file = new File(rootDirPath);
        this.rootDirPath = file.getCanonicalPath().replace("\\", "/").replaceAll("/$", "");
    }

    public String[] getFileExt() {
        return fileExt;
    }

    public void setFileExt(String[] fileExt) {
        this.fileExt = fileExt;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public Charset getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(Charset fileEncoding) {
        this.fileEncoding = fileEncoding;
    }
}
