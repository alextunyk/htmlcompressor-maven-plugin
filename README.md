Maven HTMLCompressor Plugin
===========================

[![BuildStatus](<https://travis-ci.org/hazendaz/htmlcompressor-maven-plugin.svg?branch=master>)](<https://travis-ci.org/hazendaz/htmlcompressor-maven-plugin>)
[![Mavencentral](<https://maven-badges.herokuapp.com/maven-central/com.tunyk.mvn.plugins.htmlcompressor/htmlcompressor-maven-plugin/badge.svg>)](<https://maven-badges.herokuapp.com/maven-central/com.tunyk.mvn.plugins.htmlcompressor/htmlcompressor-maven-plugin>)
[![Apache2](<http://img.shields.io/badge/license-Apache%202-blue.svg>)](<http://www.apache.org/licenses/LICENSE-2.0>)

[![endorse](http://api.coderwall.com/alextunyk/endorsecount.png)](http://coderwall.com/alextunyk)

------------------------------------------------------------------------
| Version   | 1.4-SNAPSHOT                                             |
| --------- | -------------------------------------------------------- |
| Keywords  | HTML/XML compression, htmlcompressor, Java, Maven plugin |
| Copyright | Alex Tunyk <alex@tunyk.com>                              |
| License   | Apache License version 2.0                               |
------------------------------------------------------------------------

Maven HTMLCompressor Plugin allows to compress HTML/XML files by adding a few lines to the pom file.
This plugin uses [htmlcompressor][] library.

Usage
-----

The simplest way to start using this plugin is:

1.Enable plugin in your pom.xml

``` xml
<build>
    <plugins>
        <plugin>
            <groupId>com.tunyk.mvn.plugins.htmlcompressor</groupId>
            <artifactId>htmlcompressor-maven-plugin</artifactId>
            <version>1.4-SNAPSHOT</version>
            <configuration>
                <goalPrefix>htmlcompressor</goalPrefix>
            </configuration>
        </plugin>
    </plugins>
</build>
```

2.Put XML and HTML files under `src/main/resources` into any underlying
structure as HTMLCompressor will recursively process files

3.For HTML compression, create `integration.js` file under
`src/main/resources` where html is stored with the contents like below.
It will integrate HTML templates into JavaScript (%s will be replaced
with JSON object and copied to the target folder).

``` java
var htmlTemplatesInjector = {
    htmlTemplates: %s
};
```

4.Run maven goals:

```
mvn htmlcompressor:html
mvn htmlcompressor:xml
```

5.Check the target folder for output where resources are stored.

Here is [demo you can download][]

More information about HTML/XML compression plugin configuration at
[wiki page][]:

Source
------

The source code is available on GitHub [htmlcompressor-maven-plugin][]

```
git clone https://github.com/TUNYK/htmlcompressor-maven-plugin.git
```

Issues tracking
---------------

Issues tracking is available on [GitHub issues][]

Bug reports, feature requests, and general inquiries welcome.

  [htmlcompressor]: https://code.google.com/p/htmlcompressor
  [demo you can download]: https://github.com/TUNYK/htmlcompressor-maven-plugin/downloads
  [wiki page]: https://github.com/TUNYK/htmlcompressor-maven-plugin/wiki/Configuration
  [htmlcompressor-maven-plugin]: https://github.com/TUNYK/htmlcompressor-maven-plugin
  [GitHub issues]: https://github.com/TUNYK/htmlcompressor-maven-plugin/issues