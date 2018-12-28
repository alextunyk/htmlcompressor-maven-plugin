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
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Compress HTML files
 *
 * @goal html
 */
public class HtmlCompressorMojo extends AbstractMojo {

    /**
     * file where statistics of html compression is stored
     *
     * @parameter property="htmlcompressor.htmlCompressionStatistics" default-value="${project.build.directory}/htmlcompressor/html-compression-statistics.txt"
     */
    private String htmlCompressionStatistics = "target/htmlcompressor/html-compression-statistics.txt";

    /**
     * file types to be processed
     *
     * @parameter property="htmlcompressor.fileExt"
     */
    private String[] fileExt;
    
    /**
     * if false all compression is off (default is true)
     *
     * @parameter property="htmlcompressor.enabled" default-value="true"
     */
    private Boolean enabled = true;

    /**
     * if false keeps HTML comments (default is true)
     *
     * @parameter property="htmlcompressor.removeComments" default-value="true"
     */
    private Boolean removeComments = true;

    /**
     * if false keeps multiple whitespace characters (default is true)
     *
     * @parameter property="htmlcompressor.removeMultiSpaces" default-value="true"
     */
    private Boolean removeMultiSpaces = true;

    /**
     * removes iter-tag whitespace characters
     *
     * @parameter property="htmlcompressor.removeIntertagSpaces" default-value="false"
     */
    private Boolean removeIntertagSpaces = false;

    /**
     * removes unnecessary tag attribute quotes
     *
     * @parameter property="htmlcompressor.removeQuotes" default-value="false"
     */
    private Boolean removeQuotes = false;

    /**
     * simplify existing doctype
     *
     * @parameter property="htmlcompressor.simpleDoctype" default-value="false"
     */
    private Boolean simpleDoctype = false;

    /**
     * remove optional attributes from script tags
     *
     * @parameter property="htmlcompressor.removeScriptAttributes" default-value="false"
     */
    private Boolean removeScriptAttributes = false;

    /**
     * remove optional attributes from style tags
     *
     * @parameter property="htmlcompressor.removeStyleAttributes" default-value="false"
     */
    private Boolean removeStyleAttributes = false;

    /**
     * remove optional attributes from link tags
     *
     * @parameter property="htmlcompressor.removeLinkAttributes" default-value="false"
     */
    private Boolean removeLinkAttributes = false;

    /**
     * remove optional attributes from form tags
     *
     * @parameter property="htmlcompressor.removeFormAttributes" default-value="false"
     */
    private Boolean removeFormAttributes = false;

    /**
     * remove optional attributes from input tags
     *
     * @parameter property="htmlcompressor.removeInputAttributes" default-value="false"
     */
    private Boolean removeInputAttributes = false;

    /**
     * remove values from boolean tag attributes
     *
     * @parameter property="htmlcompressor.simpleBooleanAttributes" default-value="false"
     */
    private Boolean simpleBooleanAttributes = false;

    /**
     * remove "javascript:" from inline event handlers
     *
     * @parameter property="htmlcompressor.removeJavaScriptProtocol" default-value="false"
     */
    private Boolean removeJavaScriptProtocol = false;

    /**
     * replace "http://" with "//" inside tag attributes
     *
     * @parameter property="htmlcompressor.removeHttpProtocol" default-value="false"
     */
    private Boolean removeHttpProtocol = false;

    /**
     * replace "https://" with "//" inside tag attributes
     *
     * @parameter property="htmlcompressor.removeHttpsProtocol" default-value="false"
     */
    private Boolean removeHttpsProtocol = false;

    /**
     * compress inline css
     *
     * @parameter property="htmlcompressor.compressCss" default-value="false"
     */
    private Boolean compressCss = false;

    /**
     * preserves original line breaks
     *
     * @parameter property="htmlcompressor.preserveLineBreaks" default-value="false"
     */
    private Boolean preserveLineBreaks = false;

    /**
     * --line-break param for Yahoo YUI Compressor
     *
     * @parameter property="htmlcompressor.yuiCssLineBreak" default-value="-1"
     */
    private Integer yuiCssLineBreak = -1;

    /**
     * css compressor
     *
     * @parameter property="htmlcompressor.cssCompressor" default-value=""
     */
    //private Compressor cssCompressor;

    /**
     * compress inline javascript
     *
     * @parameter property="htmlcompressor.compressJavaScript" default-value="false"
     */
    private Boolean compressJavaScript = false;

    /**
     * javascript compression: "yui" or "closure"
     *
     * @parameter property="htmlcompressor.jsCompressor" default-value="yui"
     */
    private String jsCompressor = "yui";

    /**
     * javascript compression
     *
     * @parameter property="htmlcompressor.javaScriptCompressor" default-value=""
     */
    //private Compressor javaScriptCompressor; // TODO: provide ability to specify class for javaScriptCompressor param

    /**
     * --nomunge param for Yahoo YUI Compressor
     *
     * @parameter property="htmlcompressor.yuiJsNoMunge" default-value="false"
     */
    private Boolean yuiJsNoMunge = false;

    /**
     * --preserve-semi param for Yahoo YUI Compressor
     *
     * @parameter property="htmlcompressor.yuiJsPreserveAllSemiColons" default-value="false"
     */
    private Boolean yuiJsPreserveAllSemiColons = false;

    /**
     * --line-break param for Yahoo YUI Compressor
     *
     * @parameter property="htmlcompressor.yuiJsLineBreak" default-value="-1"
     */
    private Integer yuiJsLineBreak = -1;

    /**
     * closureOptLevel = "simple", "advanced" or "whitespace"
     *
     * @parameter property="htmlcompressor.closureOptLevel" default-value="simple"
     */
    private String closureOptLevel = "simple";

    /**
     * --disable-optimizations param for Yahoo YUI Compressor
     *
     * @parameter property="htmlcompressor.yuiJsDisableOptimizations" default-value="false"
     */
    private Boolean yuiJsDisableOptimizations = false;

    /**
     * predefined patterns for most often used custom preservation rules: PHP_TAG_PATTERN and SERVER_SCRIPT_TAG_PATTERN.
     *
     * @parameter property="htmlcompressor.predefinedPreservePatterns"
     */
    private String[] predefinedPreservePatterns;

    /**
     * preserve patterns
     *
     * @parameter property="htmlcompressor.preservePatterns"
     */
    private String[] preservePatterns;

    /**
     * list of files containing preserve patterns
     *
     * @parameter property="htmlcompressor.preservePatternFiles"
     */
    private File[] preservePatternFiles;

    /**
     * HTML compression statistics
     *
     * @parameter property="htmlcompressor.generateStatistics" default-value="true"
     */
    private Boolean generateStatistics = true;

    /**
     * source folder where html files are located.
     *
     * @parameter property="htmlcompressor.srcFolder" default-value="${basedir}/src/main/resources"
     */
    private String srcFolder = "src/main/resources";

    /**
     * target folder where compressed html files will be placed.
     *
     * @parameter property="htmlcompressor.targetFolder" default-value="${project.build.directory}/classes"
     */
    private String targetFolder = "target/classes";

    /**
     * Create javascript file which includes all compressed html files as json object. If set to true then javascriptHtmlSpriteIntegrationFile param is required, otherwise it will throw exception.
     *
     * @parameter property="htmlcompressor.javascriptHtmlSprite" default-value="true"
     */
    private Boolean javascriptHtmlSprite = true;

    /**
     * JavaScript sprite integration file (first occurrence of "%s" will be substituted by json with all compressed html strings)
     *
     * @parameter property="htmlcompressor.javascriptHtmlSpriteIntegrationFile" default-value="${basedir}/src/main/resources/html/integration.js"
     */
    private String javascriptHtmlSpriteIntegrationFile = "src/main/resources/html/integration.js";

    /**
     * The target JavaScript sprite file with compressed html files as json object.
     *
     * @parameter property="{htmlcompressor.javascriptHtmlSpriteTargetFile" default-value="${project.build.directory}/htmlcompressor/html/integration.js"
     */
    private String javascriptHtmlSpriteTargetFile = "target/htmlcompressor/html/integration.js";

    /**
     * Charset encoding for files to read and create
     *
     * @parameter property="htmlcompressor.encoding" default-value="utf-8"
     */
    private String encoding = "utf-8";

    /**
     * Disable default built-in closure externs.
     *
     * @parameter property="htmlcompressor.closureCustomExternsOnly" default-value="false"
     */
    private Boolean closureCustomExternsOnly = false;

    /**
     * Sets custom closure externs file list.
     *
     * @parameter property="htmlcompressor.closureExterns"
     */
    private String[] closureExterns;

    @Override
    public void execute() throws MojoExecutionException {
        if (!enabled) {
            getLog().info("HTML compression was turned off.");
            return;
        }

        getLog().info("Compressing " + srcFolder);
        HtmlCompressor htmlCompressor = new HtmlCompressor(srcFolder, targetFolder);

        htmlCompressor.setFileExt(fileExt);
        htmlCompressor.setFileEncoding(encoding);
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
                closureCompressor.setCustomExternsOnly(closureCustomExternsOnly != null);
                if(closureExterns.length  > 0) {
                    List<SourceFile> externs = new ArrayList<SourceFile>();
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

        List<Pattern> preservePatternList = new ArrayList<Pattern>();
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
                } catch (PatternSyntaxException e) {
                    throw new MojoExecutionException(e.getMessage());
                } catch (IOException e) {
                    throw new MojoExecutionException(e.getMessage());
                }
            }
        }
        htmlCompressorHandler.setPreservePatterns(preservePatternList);
        htmlCompressor.setHtmlCompressor(htmlCompressorHandler);

        try {
            htmlCompressor.compress();
        } catch(Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }

        boolean si = true;

        // TODO: if no files matched pattern (*.htm or *.html) then this gives NullPointerException
        int origFilesizeBytes = htmlCompressor.getHtmlCompressor().getStatistics().getOriginalMetrics().getFilesize();
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
        Float compressionRatio = new Float(compFilesizeBytes) / new Float(origFilesizeBytes);
        Float spaceSavings = new Float(1) - compressionRatio;

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
            FileUtils.writeStringToFile(new File(htmlCompressionStatistics), statistics, encoding);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        getLog().info("HTML compression completed.");
    }

    public String getHtmlCompressionStatistics() {
        return htmlCompressionStatistics;
    }

    public void setHtmlCompressionStatistics(String htmlCompressionStatistics) {
        this.htmlCompressionStatistics = htmlCompressionStatistics;
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

    public Boolean getRemoveMultiSpaces() {
        return removeMultiSpaces;
    }

    public void setRemoveMultiSpaces(Boolean removeMultiSpaces) {
        this.removeMultiSpaces = removeMultiSpaces;
    }

    public Boolean getRemoveIntertagSpaces() {
        return removeIntertagSpaces;
    }

    public void setRemoveIntertagSpaces(Boolean removeIntertagSpaces) {
        this.removeIntertagSpaces = removeIntertagSpaces;
    }

    public Boolean getRemoveQuotes() {
        return removeQuotes;
    }

    public void setRemoveQuotes(Boolean removeQuotes) {
        this.removeQuotes = removeQuotes;
    }

    public Boolean getSimpleDoctype() {
        return simpleDoctype;
    }

    public void setSimpleDoctype(Boolean simpleDoctype) {
        this.simpleDoctype = simpleDoctype;
    }

    public Boolean getRemoveScriptAttributes() {
        return removeScriptAttributes;
    }

    public void setRemoveScriptAttributes(Boolean removeScriptAttributes) {
        this.removeScriptAttributes = removeScriptAttributes;
    }

    public Boolean getRemoveStyleAttributes() {
        return removeStyleAttributes;
    }

    public void setRemoveStyleAttributes(Boolean removeStyleAttributes) {
        this.removeStyleAttributes = removeStyleAttributes;
    }

    public Boolean getRemoveLinkAttributes() {
        return removeLinkAttributes;
    }

    public void setRemoveLinkAttributes(Boolean removeLinkAttributes) {
        this.removeLinkAttributes = removeLinkAttributes;
    }

    public Boolean getRemoveFormAttributes() {
        return removeFormAttributes;
    }

    public void setRemoveFormAttributes(Boolean removeFormAttributes) {
        this.removeFormAttributes = removeFormAttributes;
    }

    public Boolean getRemoveInputAttributes() {
        return removeInputAttributes;
    }

    public void setRemoveInputAttributes(Boolean removeInputAttributes) {
        this.removeInputAttributes = removeInputAttributes;
    }

    public Boolean getSimpleBooleanAttributes() {
        return simpleBooleanAttributes;
    }

    public void setSimpleBooleanAttributes(Boolean simpleBooleanAttributes) {
        this.simpleBooleanAttributes = simpleBooleanAttributes;
    }

    public Boolean getRemoveJavaScriptProtocol() {
        return removeJavaScriptProtocol;
    }

    public void setRemoveJavaScriptProtocol(Boolean removeJavaScriptProtocol) {
        this.removeJavaScriptProtocol = removeJavaScriptProtocol;
    }

    public Boolean getRemoveHttpProtocol() {
        return removeHttpProtocol;
    }

    public void setRemoveHttpProtocol(Boolean removeHttpProtocol) {
        this.removeHttpProtocol = removeHttpProtocol;
    }

    public Boolean getRemoveHttpsProtocol() {
        return removeHttpsProtocol;
    }

    public void setRemoveHttpsProtocol(Boolean removeHttpsProtocol) {
        this.removeHttpsProtocol = removeHttpsProtocol;
    }

    public Boolean getCompressCss() {
        return compressCss;
    }

    public void setCompressCss(Boolean compressCss) {
        this.compressCss = compressCss;
    }

    public Boolean getPreserveLineBreaks() {
        return preserveLineBreaks;
    }

    public void setPreserveLineBreaks(Boolean preserveLineBreaks) {
        this.preserveLineBreaks = preserveLineBreaks;
    }

    public Integer getYuiCssLineBreak() {
        return yuiCssLineBreak;
    }

    public void setYuiCssLineBreak(Integer yuiCssLineBreak) {
        this.yuiCssLineBreak = yuiCssLineBreak;
    }

    public Boolean getCompressJavaScript() {
        return compressJavaScript;
    }

    public void setCompressJavaScript(Boolean compressJavaScript) {
        this.compressJavaScript = compressJavaScript;
    }

    public String getJsCompressor() {
        return jsCompressor;
    }

    public void setJsCompressor(String jsCompressor) {
        this.jsCompressor = jsCompressor;
    }

    public Boolean getYuiJsNoMunge() {
        return yuiJsNoMunge;
    }

    public void setYuiJsNoMunge(Boolean yuiJsNoMunge) {
        this.yuiJsNoMunge = yuiJsNoMunge;
    }

    public Boolean getYuiJsPreserveAllSemiColons() {
        return yuiJsPreserveAllSemiColons;
    }

    public void setYuiJsPreserveAllSemiColons(Boolean yuiJsPreserveAllSemiColons) {
        this.yuiJsPreserveAllSemiColons = yuiJsPreserveAllSemiColons;
    }

    public Integer getYuiJsLineBreak() {
        return yuiJsLineBreak;
    }

    public void setYuiJsLineBreak(Integer yuiJsLineBreak) {
        this.yuiJsLineBreak = yuiJsLineBreak;
    }

    public String getClosureOptLevel() {
        return closureOptLevel;
    }

    public void setClosureOptLevel(String closureOptLevel) {
        this.closureOptLevel = closureOptLevel;
    }

    public Boolean getYuiJsDisableOptimizations() {
        return yuiJsDisableOptimizations;
    }

    public void setYuiJsDisableOptimizations(Boolean yuiJsDisableOptimizations) {
        this.yuiJsDisableOptimizations = yuiJsDisableOptimizations;
    }

    public String[] getPredefinedPreservePatterns() {
        return predefinedPreservePatterns;
    }

    public void setPredefinedPreservePatterns(String[] predefinedPreservePatterns) {
        this.predefinedPreservePatterns = predefinedPreservePatterns;
    }

    public String[] getPreservePatterns() {
        return preservePatterns;
    }

    public void setPreservePatterns(String[] preservePatterns) {
        this.preservePatterns = preservePatterns;
    }

    public File[] getPreservePatternFiles() {
        return preservePatternFiles;
    }

    public void setPreservePatternFiles(File[] preservePatternFiles) {
        this.preservePatternFiles = preservePatternFiles;
    }

    public Boolean getGenerateStatistics() {
        return generateStatistics;
    }

    public void setGenerateStatistics(Boolean generateStatistics) {
        this.generateStatistics = generateStatistics;
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

    public Boolean getJavascriptHtmlSprite() {
        return javascriptHtmlSprite;
    }

    public void setJavascriptHtmlSprite(Boolean javascriptHtmlSprite) {
        this.javascriptHtmlSprite = javascriptHtmlSprite;
    }

    public String getJavascriptHtmlSpriteIntegrationFile() {
        return javascriptHtmlSpriteIntegrationFile;
    }

    public void setJavascriptHtmlSpriteIntegrationFile(String javascriptHtmlSpriteIntegrationFile) {
        this.javascriptHtmlSpriteIntegrationFile = javascriptHtmlSpriteIntegrationFile;
    }

    public String getJavascriptHtmlSpriteTargetFile() {
        return javascriptHtmlSpriteTargetFile;
    }

    public void setJavascriptHtmlSpriteTargetFile(String javascriptHtmlSpriteTargetFile) {
        this.javascriptHtmlSpriteTargetFile = javascriptHtmlSpriteTargetFile;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Boolean getClosureCustomExternsOnly() {
        return closureCustomExternsOnly;
    }

    public void setClosureCustomExternsOnly(Boolean closureCustomExternsOnly) {
        this.closureCustomExternsOnly = closureCustomExternsOnly;
    }

    public String[] getClosureExterns() {
        return closureExterns;
    }

    public void setClosureExterns(String[] closureExterns) {
        this.closureExterns = closureExterns;
    }
}
