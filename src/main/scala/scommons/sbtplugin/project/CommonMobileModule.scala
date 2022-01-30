package scommons.sbtplugin.project

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.ScommonsPlugin.autoImport._
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

trait CommonMobileModule extends CommonModule {

  def scommonsReactNativeVersion: String

  override def definition: Project = {
    super.definition
      .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
      .settings(CommonMobileModule.settings: _*)
      .settings(
        // we substitute references to react-native modules with our custom mocks during test
        scommonsNodeJsTestLibs := Seq("scommons.reactnative.aliases.js")
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-react", "scommons-react-core", None),
    ("scommons-react-native", "scommons-react-native-core", None),

    ("scommons-react", "scommons-react-test", Some("test")),
    ("scommons-react-native", "scommons-react-native-test", Some("test"))
  )

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.react-native" %%% "scommons-react-native-core" % scommonsReactNativeVersion
  ))

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq[ModuleID](
    "org.scommons.react-native" %%% "scommons-react-native-test" % scommonsReactNativeVersion
  ).map(_  % "test"))
}

object CommonMobileModule {

  val settings: Seq[Setting[_]] = Seq(

    scommonsResourcesArtifacts := Seq(
      "org.scommons.react" % "scommons-react-core" % "*",
      "org.scommons.react-native" % "scommons-react-native-test" % "*"
    ),

    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
        .withSourceMap(false)
        .withESFeatures(_.withUseECMAScript2015(false))
    },
    scalaJSUseMainModuleInitializer := false,
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    
    // react-native DO NOT require DOM
    requireJsDomEnv in Test := false,
    version in webpack := "4.29.0",
    webpackEmitSourceMaps := false,
    parallelExecution in Test := false,

    npmDevDependencies in Test ++= Seq(
      "module-alias" -> "2.2.2"
    ),

    // required for node.js >= v12.12.0
    // see:
    //   https://github.com/nodejs/node/pull/29919
    scalaJSLinkerConfig in Test ~= {
      _.withSourceMap(true)
    },
    jsEnv in Test := new NodeJSEnv(NodeJSEnv.Config().withArgs(List("--enable-source-maps"))),

    ideExcludedDirectories ++= {
      val base = baseDirectory.value
      List(
        base / "build",
        base / "android" / "build",
        base / "ios" / "build",
        base / "ios" / "Pods",
        base / ".expo",
        base / "node_modules"
      )
    }
  )
}
