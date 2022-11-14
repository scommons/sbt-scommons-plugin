
import org.scalajs.linker.interface.ESVersion
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
    scalaVersion := "2.13.8",

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
        .withESFeatures(_.withESVersion(ESVersion.ES5_1))
    },
    scalaJSUseMainModuleInitializer := true,
    //webpackBundlingMode := BundlingMode.LibraryOnly(),
    webpack / version := "5.74.0",
    fastOptJS / webpackConfigFile := Some(baseDirectory.value / "client.webpack.config.js"),
    fullOptJS / webpackConfigFile := Some(baseDirectory.value / "client.webpack.config.js"),
    scommonsRequireWebpackInTest := true,
    Test / webpackConfigFile := Some(baseDirectory.value / "test.webpack.config.js"),
    webpackEmitSourceMaps := false,

    Compile / npmDevDependencies ++= Seq(
      "css-loader" -> "6.7.2",
      "mini-css-extract-plugin" -> "2.6.1",
      "webpack-node-externals" -> "3.0.0",
      "webpack-merge" -> "5.8.0"
    )
  )

lazy val server = (project in file("server"))
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.13.8",

    libraryDependencies ++= Seq(
      "org.scaldi" %% "scaldi-play" % "0.6.1",
      PlayImport.guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.14" % Test
    ),

    scalaJSProjects := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline)
  )
