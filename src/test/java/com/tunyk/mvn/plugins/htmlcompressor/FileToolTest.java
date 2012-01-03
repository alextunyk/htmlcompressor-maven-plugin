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
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Tunyk <alex at tunyk.com>
 */
public class FileToolTest {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger(FileToolTest.class);


    @BeforeClass
    public static void setUpClass() {
        LOG.info("Setting up class...");
    }

    @AfterClass
    public static void tearDownClass() {
        LOG.info("Test finished.");
    }

    @Before
    public void setUp() {
        LOG.info("Setting up data for testing...");
    }

    @Test
    public void testGetFiles() throws IOException {
        LOG.info("Testing getFiles method...");

        FileTool fileTool = new FileTool("src/test/resources/html", new String[] {"htm", "html"}, true);
        Map<String, String> map = fileTool.getFiles();

        Assert.assertTrue(map.containsKey("templates/Template1.html"));
        Assert.assertTrue(map.containsKey("templates/Template2.html"));
        Assert.assertTrue(map.containsKey("templates/recursive/Template.html"));

        // TODO: check if any other files aren't included
        // TODO: check if file ext filter works
        // TODO: check if recursion is forking
        // TODO: test file encoding

        LOG.info("Passed");
    }

    @Test
    public void testWriteFiles() throws IOException {
        LOG.info("Testing writeFiles method...");

        String targetDir = "target/test/filetool";
        FileTool fileTool = new FileTool(targetDir, new String[] {"htm", "html"}, true);
        Map<String, String> map = new HashMap<String, String>();
        map.put("file.html", "root file");
        map.put("/file2.html", "another root file");
        map.put("file3.html/", "another root file like folder");
        map.put("/template/file.html", "template file");
        map.put("template/file01.html", "template file 01");
        map.put("/template/subfolder/file.html", "template subfolder file");
        fileTool.writeFiles(map, targetDir);

        Map<String, String> files = fileTool.getFiles();
        Assert.assertTrue(files.containsKey("file.html"));
        Assert.assertTrue(files.containsKey("file2.html"));
        Assert.assertTrue(files.containsKey("file3.html"));
        Assert.assertTrue(files.containsKey("template/file.html"));
        Assert.assertTrue(files.containsKey("template/file01.html"));
        Assert.assertTrue(files.containsKey("template/subfolder/file.html"));

        // TODO: check file exists and its contents is written correctly

        LOG.info("Passed");
    }

    @Test
    public void testWriteToJsonFile() throws IOException, JSONException {
        LOG.info("Testing writeToJsonFile method...");

        String targetDir = "target/test/filetool/";
        String targetFile = targetDir + "json.js";
        FileTool fileTool = new FileTool(targetDir, new String[] {"htm", "html"}, true);
        Map<String, String> map = new HashMap<String, String>();
        map.put("file.html", "root file");
        map.put("template/file.html", "template file");
        map.put("template/subfolder/file.html", "template subfolder file");
        fileTool.writeToJsonFile(map, targetFile, "var templates = %s;");

        // TODO: check if file created and contents is right

        LOG.info("Passed");
    }
}
