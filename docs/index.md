---
layout: default
---

Welcome to the **Scala Commons Sbt Plugin** documentation page!

## Overview

![Overview](drawio/overview.svg)
[Preview](https://www.draw.io/?chrome=0&lightbox=1&url=https%3A%2F%2Fraw.githubusercontent.com%2Fscommons%2Fsbt-scommons-plugin%2Fmaster%2Fdocs%2Fdrawio%2Foverview.svg%3Ft%3D0) | [Edit](https://www.draw.io/?title=overview.svg&url=https%3A%2F%2Fraw.githubusercontent.com%2Fscommons%2Fsbt-scommons-plugin%2Fmaster%2Fdocs%2Fdrawio%2Foverview.svg%3Ft%3D0)

## ScommonsPlugin

The main purpose of this sbt `auto-plugin` is to copy/extract resource files
(`css`, `png`, `jpeg`) from project dependencies to the `webpack` folder
during the `fastOptJS/fullOptJS` `scalajs` task, so they can be used later
by the `webpack` task.

This is very similar to copying/extracting of `*.js` files,
which is already provided by the `ScalaJSBundlerPlugin`.

Since this plugin is sbt `auto-plugin`, it will be automatically activated
for all the projects that depend on `ScalaJSBundlerPlugin`.

It provides the following settings:
```scala
val scommonsResourcesFileFilter: SettingKey[FileFilter] = settingKey[FileFilter](
  "File filter of resources files, that should be automatically copied/extracted to the webpack directory"
)
val scommonsResourcesArtifacts: SettingKey[Seq[ModuleID]] = settingKey[Seq[ModuleID]](
  "List of artifacts (JARs) with resources, that should be automatically extracted to the webpack directory"
)

val scommonsBundlesFileFilter: SettingKey[FileFilter] = settingKey[FileFilter](
  "File filter of bundles files, that should be automatically generated in the webpack directory"
)

val scommonsNodeJsTestLibs: SettingKey[Seq[String]] = settingKey[Seq[String]](
  "List of JavaScript files, that should be pre-pended to the test fastOptJS output, useful for module mocks"
)

val scommonsRequireWebpackInTest: SettingKey[Boolean] = settingKey[Boolean](
  "Whether webpack command should be executed during tests, use webpackConfigFile for custom configuration"
)
```

With default values:
```scala
scommonsResourcesFileFilter :=
  "*.js" ||
    "*.json" ||
    "*.css" ||
    "*.ico" ||
    "*.png" ||
    "*.jpg" ||
    "*.jpeg" ||
    "*.gif" ||
    "*.svg" ||
    "*.ttf" ||
    "*.mp3" ||
    "*.wav" ||
    "*.mp4" ||
    "*.mov" ||
    "*.html" ||
    "*.pdf",

scommonsResourcesArtifacts := Seq(
  "org.scommons.shogowada" % "scalajs-reactjs-core" % "*",
  "org.scommons.react" % "scommons-react-core" % "*",
  "org.scommons.client" % "scommons-client-ui" % "*"
),

scommonsBundlesFileFilter := NothingFilter,

scommonsNodeJsTestLibs := Nil

scommonsRequireWebpackInTest := false,

version in webpack := "4.29.0"
```

You can extend/override the default values:
```scala
settings(
    scommonsResourcesFileFilter :=
        scommonsResourcesFileFilter.value || "*.svg",

    scommonsResourcesArtifacts ++= Seq(
      "your.org" % "your-dependency" % "*"
    ),
    
    // will generate bundle.json file(s) with migrations for SQLite
    // see `scommons-websql-migrations` module
    scommonsBundlesFileFilter := "*.sql",
    
    // to substitute references to react-native modules
    // with our custom mocks during test
    scommonsNodeJsTestLibs := Seq("scommons.reactnative.aliases.js"),

    // to execute webpack during tests
    // (to perform css styles transformations as in main, for example)
    scommonsRequireWebpackInTest := true,
    webpackConfigFile in Test := Some(baseDirectory.value / "test.webpack.config.js"),
)
```

