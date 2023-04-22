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

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.SourceFile;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.Compressor;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Compress HTML files.
 */
@Mojo(name = "html", defaultPhase = LifecyclePhase.COMPILE, requiresProject = false, threadSafe = true)
public class HtmlCompressorMojo extends AbstractMojo {

    /** file where statistics of html compression is stored. */
    @Parameter(property="htmlcompressor.htmlCompressionStatistics", defaultValue="${project.build.directory}/htmlcompressor/html-compression-statistics.txt")
    private String htmlCompressionStatistics = "target/htmlcompressor/html-compression-statistics.txt";

    /** file types to be processed. */
    @Parameter(property="htmlcompressor.fileExt")
    private String[] fileExt;

    /** if false all compression is off (default is true). */
    @Parameter(property="htmlcompressor.enabled", defaultValue="true")
    private boolean enabled = true;

    /** Skip run of plugin. */
    @Parameter(defaultValue = "false", alias = "skip", property = "skip")
    private boolean skip;

    /** if false keeps HTML comments (default is true). */
    @Parameter(property="htmlcompressor.removeComments", defaultValue="true")
    private boolean removeComments = true;

    /** if false keeps multiple whitespace characters (default is true). */
    @Parameter(property="htmlcompressor.removeMultiSpaces", defaultValue="true")
    private boolean removeMultiSpaces = true;

    /** removes iter-tag whitespace characters. */
    @Parameter(property="htmlcompressor.removeIntertagSpaces", defaultValue="false")
    private boolean removeIntertagSpaces;

    /** removes unnecessary tag attribute quotes. */
    @Parameter(property="htmlcompressor.removeQuotes", defaultValue="false")
    private boolean removeQuotes;

    /** simplify existing doctype. */
    @Parameter(property="htmlcompressor.simpleDoctype", defaultValue="false")
    private boolean simpleDoctype;

    /** remove optional attributes from script tags. */
    @Parameter(property="htmlcompressor.removeScriptAttributes", defaultValue="false")
    private boolean removeScriptAttributes;

    /** remove optional attributes from style tags. */
    @Parameter(property="htmlcompressor.removeStyleAttributes", defaultValue="false")
    private boolean removeStyleAttributes;

    /** remove optional attributes from link tags. */
    @Parameter(property="htmlcompressor.removeLinkAttributes", defaultValue="false")
    private boolean removeLinkAttributes;

    /** remove optional attributes from form tags. */
    @Parameter(property="htmlcompressor.removeFormAttributes", defaultValue="false")
    private boolean removeFormAttributes;

    /** remove optional attributes from input tags. */
    @Parameter(property="htmlcompressor.removeInputAttributes", defaultValue="false")
    private boolean removeInputAttributes;

    /** remove values from boolean tag attributes. */
    @Parameter(property="htmlcompressor.simpleBooleanAttributes", defaultValue="false")
    private boolean simpleBooleanAttributes;

    /** remove "javascript:" from inline event handlers. */
    @Parameter(property="htmlcompressor.removeJavaScriptProtocol", defaultValue="false")
    private boolean removeJavaScriptProtocol;

    /** replace "http://" with "//" inside tag attributes. */
    @Parameter(property="htmlcompressor.removeHttpProtocol", defaultValue="false")
    private boolean removeHttpProtocol;

    /** replace "https://" with "//" inside tag attributes. */
    @Parameter(property="htmlcompressor.removeHttpsProtocol", defaultValue="false")
    private boolean removeHttpsProtocol;

    /** compress inline css. */
    @Parameter(property="htmlcompressor.compressCss", defaultValue="false")
    private boolean compressCss;

    /** preserves original line breaks. */
    @Parameter(property="htmlcompressor.preserveLineBreaks", defaultValue="false")
    private boolean preserveLineBreaks;

    /** --line-break param for Yahoo YUI Compressor. */
    @Parameter(property="htmlcompressor.yuiCssLineBreak", defaultValue="-1")
    private int yuiCssLineBreak = -1;

    /** css compressor. */
    // TODO JWL 4/22/2023 Unsupported
    @SuppressWarnings("unused")
    @Parameter(property="htmlcompressor.cssCompressor", defaultValue="")
    private Compressor cssCompressor;

    /** compress inline javascript. */
    @Parameter(property="htmlcompressor.compressJavaScript", defaultValue="false")
    private boolean compressJavaScript;

    /** javascript compression: "yui" or "closure". */
    @Parameter(property="htmlcompressor.jsCompressor", defaultValue="yui")
    private String jsCompressor = "yui";

    /** javascript compression. */
    // TODO JWL 4/22/2023Unsupported
    @SuppressWarnings("unused")
    @Parameter(property="htmlcompressor.javaScriptCompressor", defaultValue="")
    private Compressor javaScriptCompressor;

    /** --nomunge param for Yahoo YUI Compressor. */
    @Parameter(property="htmlcompressor.yuiJsNoMunge", defaultValue="false")
    private boolean yuiJsNoMunge;

    /** --preserve-semi param for Yahoo YUI Compressor. */
    @Parameter(property="htmlcompressor.yuiJsPreserveAllSemiColons", defaultValue="false")
    private boolean yuiJsPreserveAllSemiColons;

    /** --line-break param for Yahoo YUI Compressor. */
    @Parameter(property="htmlcompressor.yuiJsLineBreak", defaultValue="-1")
    private int yuiJsLineBreak = -1;

    /** closureOptLevel = "simple", "advanced" or "whitespace". */
    @Parameter(property="htmlcompressor.closureOptLevel", defaultValue="simple")
    private String closureOptLevel = "simple";

    /** --disable-optimizations param for Yahoo YUI Compressor. */
    @Parameter(property="htmlcompressor.yuiJsDisableOptimizations", defaultValue="false")
    private boolean yuiJsDisableOptimizations;

    /**
     * predefined patterns for most often used custom preservation rules: PHP_TAG_PATTERN and SERVER_SCRIPT_TAG_PATTERN.
     */
    @Parameter(property="htmlcompressor.predefinedPreservePatterns")
    private String[] predefinedPreservePatterns;

    /** preserve patterns. */
    @Parameter(property="htmlcompressor.preservePatterns")
    private String[] preservePatterns;

    /** list of files containing preserve patterns. */
    @Parameter(property="htmlcompressor.preservePatternFiles")
    private File[] preservePatternFiles;

    /** HTML compression statistics. */
    @Parameter(property="htmlcompressor.generateStatistics", defaultValue="true")
    private boolean generateStatistics = true;

    /**
     * source folder where html files are located.
     */
    @Parameter(property="htmlcompressor.srcFolder", defaultValue="${basedir}/src/main/resources")
    private String srcFolder = "src/main/resources";

    /**
     * target folder where compressed html files will be placed.
     */
    @Parameter(property="htmlcompressor.targetFolder", defaultValue="${project.build.directory}/classes")
    private String targetFolder = "target/classes";

    /**
     * Create javascript file which includes all compressed html files as json object. If set to true then javascriptHtmlSpriteIntegrationFile param is required, otherwise it will throw exception.
     */
    @Parameter(property="htmlcompressor.javascriptHtmlSprite", defaultValue="true")
    private boolean javascriptHtmlSprite = true;

    /** JavaScript sprite integration file (first occurrence of "%s" will be substituted by json with all compressed html strings). */
    @Parameter(property="htmlcompressor.javascriptHtmlSpriteIntegrationFile", defaultValue="${basedir}/src/main/resources/html/integration.js")
    private String javascriptHtmlSpriteIntegrationFile = "src/main/resources/html/integration.js";

    /**
     * The target JavaScript sprite file with compressed html files as json object.
     */
    @Parameter(property="htmlcompressor.javascriptHtmlSpriteTargetFile", defaultValue="${project.build.directory}/htmlcompressor/html/integration.js")
    private String javascriptHtmlSpriteTargetFile = "target/htmlcompressor/html/integration.js";

    /** Charset encoding for files to read and create. */
    @Parameter(property="htmlcompressor.encoding", defaultValue="UTF-8")
    private String encoding = "UTF-8";

    /**
     * Disable default built-in closure externs.
     */
    @Parameter(property="htmlcompressor.closureCustomExternsOnly", defaultValue="false")
    private boolean closureCustomExternsOnly;

    /**
     * Sets custom closure externs file list.
     */
    @Parameter(property="htmlcompressor.closureExterns")
    private String[] closureExterns;

    @Override
    public void execute() throws MojoExecutionException {
        // Check if plugin run should be skipped
        if (this.skip) {
            getLog().info("HtmlCompressor is skipped");
            return;
        }

        if (!enabled) {
            getLog().info("HTML compression was turned off.");
            return;
        }

        if (!new File(srcFolder).exists()) {
            getLog().warn("Compressor folder does not exist, skipping compression of " + srcFolder);
            return;
        }

        getLog().info("Compressing " + srcFolder);
        HtmlCompressor htmlCompressor = new HtmlCompressor(srcFolder, targetFolder);

        htmlCompressor.setFileExt(fileExt);
        htmlCompressor.setFileEncoding(Charset.forName(encoding));
        htmlCompressor.setCreateJsonFile(javascriptHtmlSprite);
        htmlCompressor.setJsonIntegrationFilePath(javascriptHtmlSpriteIntegrationFile);
        htmlCompressor.setTargetJsonFilePath(javascriptHtmlSpriteTargetFile);

        com.googlecode.htmlcompressor.compressor.HtmlCompressor htmlCompressorHandler = new com.googlecode.htmlcompressor.compressor.HtmlCompressor();
        htmlCompressorHandler.setEnabled(enabled);
        htmlCompressorHandler.setRemoveComments(removeComments);
        htmlCompressorHandler.setRemoveMultiSpaces(removeMultiSpaces);
        htmlCompressorHandler.setRemoveIntertagSpaces(removeIntertagSpaces);
        htmlCompressorHandler.setRemoveQuotes(removeQuotes);
        htmlCompressorHandler.setSimpleDoctype(simpleDoctype);
        htmlCompressorHandler.setRemoveScriptAttributes(removeScriptAttributes);
        htmlCompressorHandler.setRemoveStyleAttributes(removeStyleAttributes);
        htmlCompressorHandler.setRemoveLinkAttributes(removeLinkAttributes);
        htmlCompressorHandler.setRemoveFormAttributes(removeFormAttributes);
        htmlCompressorHandler.setRemoveInputAttributes(removeInputAttributes);
        htmlCompressorHandler.setSimpleBooleanAttributes(simpleBooleanAttributes);
        htmlCompressorHandler.setRemoveJavaScriptProtocol(removeJavaScriptProtocol);
        htmlCompressorHandler.setRemoveHttpProtocol(removeHttpProtocol);
        htmlCompressorHandler.setRemoveHttpsProtocol(removeHttpsProtocol);
        htmlCompressorHandler.setCompressCss(compressCss);
        htmlCompressorHandler.setPreserveLineBreaks(preserveLineBreaks);
        htmlCompressorHandler.setYuiCssLineBreak(yuiCssLineBreak);
        htmlCompressorHandler.setCompressJavaScript(compressJavaScript);
        htmlCompressorHandler.setYuiJsNoMunge(yuiJsNoMunge);
        htmlCompressorHandler.setYuiJsPreserveAllSemiColons(yuiJsPreserveAllSemiColons);
        htmlCompressorHandler.setYuiJsLineBreak(yuiJsLineBreak);
        htmlCompressorHandler.setYuiJsDisableOptimizations(yuiJsDisableOptimizations);
        htmlCompressorHandler.setGenerateStatistics(generateStatistics);

        if (jsCompressor.equalsIgnoreCase("closure")) {
            ClosureJavaScriptCompressor closureCompressor = new ClosureJavaScriptCompressor();
            if (closureOptLevel != null && closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_ADVANCED)) {
                closureCompressor.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
                closureCompressor.setCustomExternsOnly(closureCustomExternsOnly);
                if(closureExterns.length  > 0) {
                    List<SourceFile> externs = new ArrayList<>();
                    for(String externFile : closureExterns) {
                        externs.add(SourceFile.fromFile(externFile));
                    }
                    closureCompressor.setExterns(externs);
                }
            } else if (closureOptLevel != null && closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_WHITESPACE)) {
                closureCompressor.setCompilationLevel(CompilationLevel.WHITESPACE_ONLY);
            } else {
                closureCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
            }

            htmlCompressorHandler.setJavaScriptCompressor(closureCompressor);
        }

        List<Pattern> preservePatternList = new ArrayList<>();
        boolean phpTagPatternAdded = false;
        boolean serverScriptTagPatternAdded = false;
        if (predefinedPreservePatterns != null) {
            for (String pattern : predefinedPreservePatterns) {
                if (!phpTagPatternAdded && pattern.equalsIgnoreCase("PHP_TAG_PATTERN")) {
                    preservePatternList.add(com.googlecode.htmlcompressor.compressor.HtmlCompressor.PHP_TAG_PATTERN);
                    phpTagPatternAdded = true;
                } else if (!serverScriptTagPatternAdded && pattern.equalsIgnoreCase("SERVER_SCRIPT_TAG_PATTERN")) {
                    preservePatternList.add(com.googlecode.htmlcompressor.compressor.HtmlCompressor.SERVER_SCRIPT_TAG_PATTERN);
                    serverScriptTagPatternAdded = true;
                }
            }
        }
        if (preservePatterns != null) {
            for (String preservePatternString : preservePatterns) {
                if (!preservePatternString.isEmpty()) {
                    try {
                        preservePatternList.add(Pattern.compile(preservePatternString));
                    } catch (PatternSyntaxException e) {
                        throw new MojoExecutionException(e.getMessage());
                    }
                }
            }
        }
        if (preservePatternFiles != null) {
            for (File file : preservePatternFiles) {
                try {
                    List<String> fileLines = Files.readAllLines(file.toPath(), Charset.forName(encoding));
                    for (String line : fileLines) {
                        if (!line.isEmpty()) {
                            preservePatternList.add(Pattern.compile(line));
                        }
                    }
                } catch (IOException | PatternSyntaxException e) {
                    throw new MojoExecutionException(e.getMessage());
                }
            }
        }
        htmlCompressorHandler.setPreservePatterns(preservePatternList);
        htmlCompressor.setHtmlCompressor(htmlCompressorHandler);

        try {
            htmlCompressor.compress();
        } catch(IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        boolean si = true;

        int origFilesizeBytes = -1;
        try {
            htmlCompressor.getHtmlCompressor().getStatistics().getOriginalMetrics().getFilesize();
        } catch (NullPointerException e) {
            getLog().info("No files found to compress, HTML compression completed.");
            return;
        }

        String origFilesize = FileTool.humanReadableByteCount(origFilesizeBytes, si);
        String origEmptyChars = String.valueOf(htmlCompressor.getHtmlCompressor().getStatistics().getOriginalMetrics().getEmptyChars());
        String origInlineEventSize = FileTool.humanReadableByteCount(htmlCompressor.getHtmlCompressor().getStatistics().getOriginalMetrics().getInlineEventSize(), si);
        String origInlineScriptSize = FileTool.humanReadableByteCount(htmlCompressor.getHtmlCompressor().getStatistics().getOriginalMetrics().getInlineScriptSize(), si);
        String origInlineStyleSize = FileTool.humanReadableByteCount(htmlCompressor.getHtmlCompressor().getStatistics().getOriginalMetrics().getInlineStyleSize(), si);

        int compFilesizeBytes = htmlCompressor.getHtmlCompressor().getStatistics().getCompressedMetrics().getFilesize();
        String compFilesize = FileTool.humanReadableByteCount(compFilesizeBytes, si);
        String compEmptyChars = String.valueOf(htmlCompressor.getHtmlCompressor().getStatistics().getCompressedMetrics().getEmptyChars());
        String compInlineEventSize = FileTool.humanReadableByteCount(htmlCompressor.getHtmlCompressor().getStatistics().getCompressedMetrics().getInlineEventSize(), si);
        String compInlineScriptSize = FileTool.humanReadableByteCount(htmlCompressor.getHtmlCompressor().getStatistics().getCompressedMetrics().getInlineScriptSize(), si);
        String compInlineStyleSize = FileTool.humanReadableByteCount(htmlCompressor.getHtmlCompressor().getStatistics().getCompressedMetrics().getInlineStyleSize(), si);

        String elapsedTime = FileTool.getElapsedHMSTime(htmlCompressor.getHtmlCompressor().getStatistics().getTime());
        String preservedSize = FileTool.humanReadableByteCount(htmlCompressor.getHtmlCompressor().getStatistics().getPreservedSize(), si);
        Float compressionRatio = Float.valueOf(compFilesizeBytes) / Float.valueOf(origFilesizeBytes);
        Float spaceSavings = Float.valueOf(1) - compressionRatio;

        String format = "%-30s%-30s%-30s%-2s";
        NumberFormat formatter = new DecimalFormat("#0.00");
        String eol = "\n";
        String hr = "+-----------------------------+-----------------------------+-----------------------------+";
        StringBuilder sb = new StringBuilder("HTML compression statistics:").append(eol);
        sb.append(hr).append(eol);
        sb.append(String.format(format, "| Category", "| Original", "| Compressed", "|")).append(eol);
        sb.append(hr).append(eol);
        sb.append(String.format(format, "| Filesize", "| " + origFilesize, "| " + compFilesize, "|")).append(eol);
        sb.append(String.format(format, "| Empty Chars", "| " + origEmptyChars, "| " + compEmptyChars, "|")).append(eol);
        sb.append(String.format(format, "| Script Size", "| " + origInlineScriptSize, "| " + compInlineScriptSize, "|")).append(eol);
        sb.append(String.format(format, "| Style Size", "| " + origInlineStyleSize, "| " + compInlineStyleSize, "|")).append(eol);
        sb.append(String.format(format, "| Event Handler Size", "| " + origInlineEventSize, "| " + compInlineEventSize, "|")).append(eol);
        sb.append(hr).append(eol);
        sb.append(String.format("%-90s%-2s",
                String.format("| Time: %s, Preserved: %s, Compression Ratio: %s, Savings: %s%%",
                        elapsedTime, preservedSize, formatter.format(compressionRatio), formatter.format(spaceSavings*100)),
                "|")).append(eol);
        sb.append(hr).append(eol);

        String statistics = sb.toString();
        getLog().info(statistics);
        try {
            Files.writeString(Path.of(htmlCompressionStatistics), statistics, Charset.forName(encoding));
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        getLog().info("HTML compression completed.");
    }

    /**
     * Gets the html compression statistics.
     *
     * @return the html compression statistics
     */
    public String getHtmlCompressionStatistics() {
        return htmlCompressionStatistics;
    }

    /**
     * Sets the html compression statistics.
     *
     * @param htmlCompressionStatistics the new html compression statistics
     */
    public void setHtmlCompressionStatistics(String htmlCompressionStatistics) {
        this.htmlCompressionStatistics = htmlCompressionStatistics;
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
     * Gets the removes the multi spaces.
     *
     * @return the removes the multi spaces
     */
    public Boolean getRemoveMultiSpaces() {
        return removeMultiSpaces;
    }

    /**
     * Sets the removes the multi spaces.
     *
     * @param removeMultiSpaces the new removes the multi spaces
     */
    public void setRemoveMultiSpaces(Boolean removeMultiSpaces) {
        this.removeMultiSpaces = removeMultiSpaces;
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
     * Gets the removes the quotes.
     *
     * @return the removes the quotes
     */
    public Boolean getRemoveQuotes() {
        return removeQuotes;
    }

    /**
     * Sets the removes the quotes.
     *
     * @param removeQuotes the new removes the quotes
     */
    public void setRemoveQuotes(Boolean removeQuotes) {
        this.removeQuotes = removeQuotes;
    }

    /**
     * Gets the simple doctype.
     *
     * @return the simple doctype
     */
    public Boolean getSimpleDoctype() {
        return simpleDoctype;
    }

    /**
     * Sets the simple doctype.
     *
     * @param simpleDoctype the new simple doctype
     */
    public void setSimpleDoctype(Boolean simpleDoctype) {
        this.simpleDoctype = simpleDoctype;
    }

    /**
     * Gets the removes the script attributes.
     *
     * @return the removes the script attributes
     */
    public Boolean getRemoveScriptAttributes() {
        return removeScriptAttributes;
    }

    /**
     * Sets the removes the script attributes.
     *
     * @param removeScriptAttributes the new removes the script attributes
     */
    public void setRemoveScriptAttributes(Boolean removeScriptAttributes) {
        this.removeScriptAttributes = removeScriptAttributes;
    }

    /**
     * Gets the removes the style attributes.
     *
     * @return the removes the style attributes
     */
    public Boolean getRemoveStyleAttributes() {
        return removeStyleAttributes;
    }

    /**
     * Sets the removes the style attributes.
     *
     * @param removeStyleAttributes the new removes the style attributes
     */
    public void setRemoveStyleAttributes(Boolean removeStyleAttributes) {
        this.removeStyleAttributes = removeStyleAttributes;
    }

    /**
     * Gets the removes the link attributes.
     *
     * @return the removes the link attributes
     */
    public Boolean getRemoveLinkAttributes() {
        return removeLinkAttributes;
    }

    /**
     * Sets the removes the link attributes.
     *
     * @param removeLinkAttributes the new removes the link attributes
     */
    public void setRemoveLinkAttributes(Boolean removeLinkAttributes) {
        this.removeLinkAttributes = removeLinkAttributes;
    }

    /**
     * Gets the removes the form attributes.
     *
     * @return the removes the form attributes
     */
    public Boolean getRemoveFormAttributes() {
        return removeFormAttributes;
    }

    /**
     * Sets the removes the form attributes.
     *
     * @param removeFormAttributes the new removes the form attributes
     */
    public void setRemoveFormAttributes(Boolean removeFormAttributes) {
        this.removeFormAttributes = removeFormAttributes;
    }

    /**
     * Gets the removes the input attributes.
     *
     * @return the removes the input attributes
     */
    public Boolean getRemoveInputAttributes() {
        return removeInputAttributes;
    }

    /**
     * Sets the removes the input attributes.
     *
     * @param removeInputAttributes the new removes the input attributes
     */
    public void setRemoveInputAttributes(Boolean removeInputAttributes) {
        this.removeInputAttributes = removeInputAttributes;
    }

    /**
     * Gets the simple boolean attributes.
     *
     * @return the simple boolean attributes
     */
    public Boolean getSimpleBooleanAttributes() {
        return simpleBooleanAttributes;
    }

    /**
     * Sets the simple boolean attributes.
     *
     * @param simpleBooleanAttributes the new simple boolean attributes
     */
    public void setSimpleBooleanAttributes(Boolean simpleBooleanAttributes) {
        this.simpleBooleanAttributes = simpleBooleanAttributes;
    }

    /**
     * Gets the removes the java script protocol.
     *
     * @return the removes the java script protocol
     */
    public Boolean getRemoveJavaScriptProtocol() {
        return removeJavaScriptProtocol;
    }

    /**
     * Sets the removes the java script protocol.
     *
     * @param removeJavaScriptProtocol the new removes the java script protocol
     */
    public void setRemoveJavaScriptProtocol(Boolean removeJavaScriptProtocol) {
        this.removeJavaScriptProtocol = removeJavaScriptProtocol;
    }

    /**
     * Gets the removes the http protocol.
     *
     * @return the removes the http protocol
     */
    public Boolean getRemoveHttpProtocol() {
        return removeHttpProtocol;
    }

    /**
     * Sets the removes the http protocol.
     *
     * @param removeHttpProtocol the new removes the http protocol
     */
    public void setRemoveHttpProtocol(Boolean removeHttpProtocol) {
        this.removeHttpProtocol = removeHttpProtocol;
    }

    /**
     * Gets the removes the https protocol.
     *
     * @return the removes the https protocol
     */
    public Boolean getRemoveHttpsProtocol() {
        return removeHttpsProtocol;
    }

    /**
     * Sets the removes the https protocol.
     *
     * @param removeHttpsProtocol the new removes the https protocol
     */
    public void setRemoveHttpsProtocol(Boolean removeHttpsProtocol) {
        this.removeHttpsProtocol = removeHttpsProtocol;
    }

    /**
     * Gets the compress css.
     *
     * @return the compress css
     */
    public Boolean getCompressCss() {
        return compressCss;
    }

    /**
     * Sets the compress css.
     *
     * @param compressCss the new compress css
     */
    public void setCompressCss(Boolean compressCss) {
        this.compressCss = compressCss;
    }

    /**
     * Gets the preserve line breaks.
     *
     * @return the preserve line breaks
     */
    public Boolean getPreserveLineBreaks() {
        return preserveLineBreaks;
    }

    /**
     * Sets the preserve line breaks.
     *
     * @param preserveLineBreaks the new preserve line breaks
     */
    public void setPreserveLineBreaks(Boolean preserveLineBreaks) {
        this.preserveLineBreaks = preserveLineBreaks;
    }

    /**
     * Gets the yui css line break.
     *
     * @return the yui css line break
     */
    public Integer getYuiCssLineBreak() {
        return yuiCssLineBreak;
    }

    /**
     * Sets the yui css line break.
     *
     * @param yuiCssLineBreak the new yui css line break
     */
    public void setYuiCssLineBreak(Integer yuiCssLineBreak) {
        this.yuiCssLineBreak = yuiCssLineBreak;
    }

    /**
     * Gets the compress java script.
     *
     * @return the compress java script
     */
    public Boolean getCompressJavaScript() {
        return compressJavaScript;
    }

    /**
     * Sets the compress java script.
     *
     * @param compressJavaScript the new compress java script
     */
    public void setCompressJavaScript(Boolean compressJavaScript) {
        this.compressJavaScript = compressJavaScript;
    }

    /**
     * Gets the js compressor.
     *
     * @return the js compressor
     */
    public String getJsCompressor() {
        return jsCompressor;
    }

    /**
     * Sets the js compressor.
     *
     * @param jsCompressor the new js compressor
     */
    public void setJsCompressor(String jsCompressor) {
        this.jsCompressor = jsCompressor;
    }

    /**
     * Gets the yui js no munge.
     *
     * @return the yui js no munge
     */
    public Boolean getYuiJsNoMunge() {
        return yuiJsNoMunge;
    }

    /**
     * Sets the yui js no munge.
     *
     * @param yuiJsNoMunge the new yui js no munge
     */
    public void setYuiJsNoMunge(Boolean yuiJsNoMunge) {
        this.yuiJsNoMunge = yuiJsNoMunge;
    }

    /**
     * Gets the yui js preserve all semi colons.
     *
     * @return the yui js preserve all semi colons
     */
    public Boolean getYuiJsPreserveAllSemiColons() {
        return yuiJsPreserveAllSemiColons;
    }

    /**
     * Sets the yui js preserve all semi colons.
     *
     * @param yuiJsPreserveAllSemiColons the new yui js preserve all semi colons
     */
    public void setYuiJsPreserveAllSemiColons(Boolean yuiJsPreserveAllSemiColons) {
        this.yuiJsPreserveAllSemiColons = yuiJsPreserveAllSemiColons;
    }

    /**
     * Gets the yui js line break.
     *
     * @return the yui js line break
     */
    public Integer getYuiJsLineBreak() {
        return yuiJsLineBreak;
    }

    /**
     * Sets the yui js line break.
     *
     * @param yuiJsLineBreak the new yui js line break
     */
    public void setYuiJsLineBreak(Integer yuiJsLineBreak) {
        this.yuiJsLineBreak = yuiJsLineBreak;
    }

    /**
     * Gets the closure opt level.
     *
     * @return the closure opt level
     */
    public String getClosureOptLevel() {
        return closureOptLevel;
    }

    /**
     * Sets the closure opt level.
     *
     * @param closureOptLevel the new closure opt level
     */
    public void setClosureOptLevel(String closureOptLevel) {
        this.closureOptLevel = closureOptLevel;
    }

    /**
     * Gets the yui js disable optimizations.
     *
     * @return the yui js disable optimizations
     */
    public Boolean getYuiJsDisableOptimizations() {
        return yuiJsDisableOptimizations;
    }

    /**
     * Sets the yui js disable optimizations.
     *
     * @param yuiJsDisableOptimizations the new yui js disable optimizations
     */
    public void setYuiJsDisableOptimizations(Boolean yuiJsDisableOptimizations) {
        this.yuiJsDisableOptimizations = yuiJsDisableOptimizations;
    }

    /**
     * Gets the predefined preserve patterns.
     *
     * @return the predefined preserve patterns
     */
    public String[] getPredefinedPreservePatterns() {
        return predefinedPreservePatterns;
    }

    /**
     * Sets the predefined preserve patterns.
     *
     * @param predefinedPreservePatterns the new predefined preserve patterns
     */
    public void setPredefinedPreservePatterns(String[] predefinedPreservePatterns) {
        this.predefinedPreservePatterns = predefinedPreservePatterns;
    }

    /**
     * Gets the preserve patterns.
     *
     * @return the preserve patterns
     */
    public String[] getPreservePatterns() {
        return preservePatterns;
    }

    /**
     * Sets the preserve patterns.
     *
     * @param preservePatterns the new preserve patterns
     */
    public void setPreservePatterns(String[] preservePatterns) {
        this.preservePatterns = preservePatterns;
    }

    /**
     * Gets the preserve pattern files.
     *
     * @return the preserve pattern files
     */
    public File[] getPreservePatternFiles() {
        return preservePatternFiles;
    }

    /**
     * Sets the preserve pattern files.
     *
     * @param preservePatternFiles the new preserve pattern files
     */
    public void setPreservePatternFiles(File[] preservePatternFiles) {
        this.preservePatternFiles = preservePatternFiles;
    }

    /**
     * Gets the generate statistics.
     *
     * @return the generate statistics
     */
    public Boolean getGenerateStatistics() {
        return generateStatistics;
    }

    /**
     * Sets the generate statistics.
     *
     * @param generateStatistics the new generate statistics
     */
    public void setGenerateStatistics(Boolean generateStatistics) {
        this.generateStatistics = generateStatistics;
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

    /**
     * Gets the javascript html sprite.
     *
     * @return the javascript html sprite
     */
    public Boolean getJavascriptHtmlSprite() {
        return javascriptHtmlSprite;
    }

    /**
     * Sets the javascript html sprite.
     *
     * @param javascriptHtmlSprite the new javascript html sprite
     */
    public void setJavascriptHtmlSprite(Boolean javascriptHtmlSprite) {
        this.javascriptHtmlSprite = javascriptHtmlSprite;
    }

    /**
     * Gets the javascript html sprite integration file.
     *
     * @return the javascript html sprite integration file
     */
    public String getJavascriptHtmlSpriteIntegrationFile() {
        return javascriptHtmlSpriteIntegrationFile;
    }

    /**
     * Sets the javascript html sprite integration file.
     *
     * @param javascriptHtmlSpriteIntegrationFile the new javascript html sprite integration file
     */
    public void setJavascriptHtmlSpriteIntegrationFile(String javascriptHtmlSpriteIntegrationFile) {
        this.javascriptHtmlSpriteIntegrationFile = javascriptHtmlSpriteIntegrationFile;
    }

    /**
     * Gets the javascript html sprite target file.
     *
     * @return the javascript html sprite target file
     */
    public String getJavascriptHtmlSpriteTargetFile() {
        return javascriptHtmlSpriteTargetFile;
    }

    /**
     * Sets the javascript html sprite target file.
     *
     * @param javascriptHtmlSpriteTargetFile the new javascript html sprite target file
     */
    public void setJavascriptHtmlSpriteTargetFile(String javascriptHtmlSpriteTargetFile) {
        this.javascriptHtmlSpriteTargetFile = javascriptHtmlSpriteTargetFile;
    }

    /**
     * Gets the encoding.
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding.
     *
     * @param encoding the new encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the closure custom externs only.
     *
     * @return the closure custom externs only
     */
    public Boolean getClosureCustomExternsOnly() {
        return closureCustomExternsOnly;
    }

    /**
     * Sets the closure custom externs only.
     *
     * @param closureCustomExternsOnly the new closure custom externs only
     */
    public void setClosureCustomExternsOnly(Boolean closureCustomExternsOnly) {
        this.closureCustomExternsOnly = closureCustomExternsOnly;
    }

    /**
     * Gets the closure externs.
     *
     * @return the closure externs
     */
    public String[] getClosureExterns() {
        return closureExterns;
    }

    /**
     * Sets the closure externs.
     *
     * @param closureExterns the new closure externs
     */
    public void setClosureExterns(String[] closureExterns) {
        this.closureExterns = closureExterns;
    }
}
