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

class XmlCompressorMojoTest {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger(XmlCompressorMojoTest.class);

    @BeforeAll
    static void setUpClass() {
        LOG.info("Setting up class...");
    }

    @AfterAll
    static void tearDownClass() {
        LOG.info("Mojo test finished.");
    }

    @BeforeEach
    void setUp() {
        LOG.info("Setting up data for testing...");
    }

    @Test
    void testExecute() throws MojoExecutionException {
        LOG.info("Testing mojo execution...");

        XmlCompressorMojo xmlCompressorMojo = new XmlCompressorMojo();
        xmlCompressorMojo.setSrcFolder("src/test/resources/xml");
        xmlCompressorMojo.setTargetFolder("target/htmlcompressor/xml");
        xmlCompressorMojo.execute();

        // TODO: test results

        LOG.info("Passed");
    }
}
