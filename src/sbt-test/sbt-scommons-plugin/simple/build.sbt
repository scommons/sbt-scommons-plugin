
import play.sbt._

lazy val root = (project in file("."))
  .aggregate(
    client,
    server
  )

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalaJSWeb)
  .settings(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.13.5",

    libraryDependencies ++= Seq(
      ("com.googlecode.web-commons" % "web-common-client" % "1.0.5").intransitive()
    ),

    //our plugin settings
    scommonsResourcesFileFilter := "*.css" || "*.png",
    scommonsResourcesArtifacts ++= Seq(
      "com.googlecode.web-commons" % "web-common-client" % "*"
    ),
    scommonsBundlesFileFilter := "*.sql",

    //scala.js specific settings
    scalaJSLinkerConfig ~= {
      //_.withModuleKind(ModuleKind.CommonJSModule)
      _.withSourceMap(false)
        .withESFeatures(_.withUseECMAScript2015(false))
    },
    scalaJSUseMainModuleInitializer := true,
    //webpackBundlingMode := BundlingMode.LibraryOnly(),
    webpack / version := "4.29.0",
    fastOptJS / webpackConfigFile := Some(baseDirectory.value / "client.webpack.config.js"),
    fullOptJS / webpackConfigFile := Some(baseDirectory.value / "client.webpack.config.js"),
    scommonsRequireWebpackInTest := true,
    Test / webpackConfigFile := Some(baseDirectory.value / "test.webpack.config.js"),
    webpackEmitSourceMaps := false,

    Compile / npmDevDependencies ++= Seq(
      "css-loader" -> "2.1.1",
      "mini-css-extract-plugin" -> "0.12.0",
      "resolve-url-loader" -> "3.1.2",
      "url-loader" -> "4.1.1",
      "webpack-node-externals" -> "2.5.2",
      "webpack-merge" -> "4.1.0"
    )
  )

lazy val server = (project in file("server"))
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.13.5",

    libraryDependencies ++= Seq(
      "org.scaldi" %% "scaldi-play" % "0.6.1",
      PlayImport.guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.14" % Test
    ),

    scalaJSProjects := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline)
  )
