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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class FileTool.
 */
public class FileTool {

    /** The root dir path. */
    private String rootDirPath;
    
    /** The file ext. */
    private String[] fileExt;
    
    /** The recursive. */
    private boolean recursive;
    
    /** The file encoding. */
    private Charset fileEncoding;

    /**
     * Instantiates a new file tool.
     *
     * @param rootDir the root dir
     * @param fileExt the file ext
     * @param recursive the recursive
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public FileTool(String rootDir, String[] fileExt, boolean recursive) throws IOException {
        this.setRootDirPath(rootDir);
        this.fileExt = fileExt;
        this.recursive = recursive;
    }

    /**
     * Gets the files.
     *
     * @return the files
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ConcurrentMap<String, String> getFiles() throws IOException {
        ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
        Path rootDir = Path.of(rootDirPath);
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(rootDir)) {
            paths = walk.map(Path::normalize).filter(Files::isRegularFile).filter(path -> Arrays.stream(fileExt).anyMatch(path.getFileName().toString()::endsWith)).collect(Collectors.toList());
        }
        int truncationIndex = 0;
        for (Path path : paths) {
            String normalizedFilePath = path.toFile().getCanonicalPath().replace("\\", "/");
            if (truncationIndex == 0) {
                truncationIndex = normalizedFilePath.indexOf(rootDirPath) + rootDirPath.length() + 1;
            }
            String key = normalizedFilePath.substring(truncationIndex);
            String value = Files.readString(path, getFileEncoding());
            map.put(key, value);
        }
        return map;
    }

    /**
     * Write files.
     *
     * @param map the map
     * @param targetDir the target dir
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeFiles(Map<String, String> map, String targetDir) throws IOException {
        for (Entry<String, String> entry : map.entrySet()) {
            Path path = Path.of(targetDir + '/' + entry.getKey());
            Files.createDirectories(path.getParent());
            Files.writeString(path, entry.getValue(), getFileEncoding());
        }
    }

    /**
     * Write to json file.
     *
     * @param map the map
     * @param targetFile the target file
     * @param integrationCode the integration code
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    public void writeToJsonFile(Map<String, String> map, String targetFile, String integrationCode) throws IOException, JSONException {
        String replacePattern = "%s";
        Path path = Path.of(targetFile);
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
        Files.createDirectories(path.getParent());
        Files.writeString(path, contents, getFileEncoding());
    }

    /**
     * Human readable byte count.
     *
     * @param bytes the bytes
     * @param si the si
     * @return the string
     */
    // TODO JWL 4/22/2023 Didn't see a good way to handle as it gets flagged to remove unnecessary cast if I fix this per error-prone, so ignoring it
    @SuppressWarnings("LongDoubleConversion")
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Gets the elapsed HMS time.
     *
     * @param elapsedTime the elapsed time
     * @return the elapsed HMS time
     */
    public static String getElapsedHMSTime(long elapsedTime) {
        String format = String.format("%%0%dd", 2);
        elapsedTime = elapsedTime / 1000;
        String seconds = String.format(format, elapsedTime % 60);
        String minutes = String.format(format, (elapsedTime % 3600) / 60);
        String hours = String.format(format, elapsedTime / 3600);
        return hours + ":" + minutes + ":" + seconds;
    }

    /**
     * Gets the root dir path.
     *
     * @return the root dir path
     */
    public String getRootDirPath() {
        return rootDirPath;
    }

    /**
     * Sets the root dir path.
     *
     * @param rootDirPath the new root dir path
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void setRootDirPath(String rootDirPath) throws IOException {
        File file = new File(rootDirPath);
        this.rootDirPath = file.getCanonicalPath().replace("\\", "/").replaceAll("/$", "");
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
     * Checks if is recursive.
     *
     * @return true, if is recursive
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Sets the recursive.
     *
     * @param recursive the new recursive
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Gets the file encoding.
     *
     * @return the file encoding
     */
    public Charset getFileEncoding() {
        return fileEncoding == null ? Charset.defaultCharset() : fileEncoding;
    }

    /**
     * Sets the file encoding.
     *
     * @param fileEncoding the new file encoding
     */
    public void setFileEncoding(Charset fileEncoding) {
        this.fileEncoding = fileEncoding == null ? Charset.defaultCharset() : fileEncoding;
    }
}
