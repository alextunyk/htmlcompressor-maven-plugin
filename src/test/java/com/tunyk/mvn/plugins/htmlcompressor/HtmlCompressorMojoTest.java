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

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlCompressorMojoTest {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger(HtmlCompressorMojoTest.class);

    @BeforeAll
    public static void setUpClass() {
        LOG.info("Setting up class...");
    }

    @AfterAll
    public static void tearDownClass() {
        LOG.info("Mojo test finished.");
    }

    @BeforeEach
    public void setUp() {
        LOG.info("Setting up data for testing...");
    }

    @Test
    public void testExecute() throws MojoExecutionException {
        LOG.info("Testing mojo execution...");

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setJavascriptHtmlSpriteIntegrationFile("src/test/resources/html/integration.js");
        htmlCompressorMojo.setTargetFolder("target/htmlcompressor/html");
        htmlCompressorMojo.execute();

        // TODO: test results

        LOG.info("Passed");
    }
}
