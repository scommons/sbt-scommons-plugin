package scommons.sbtplugin.project

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.ScommonsPlugin.autoImport._
import scommons.sbtplugin.project.CommonModule._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

trait CommonClientModule extends CommonModule {

  def scommonsNodejsVersion: String
  def scommonsReactVersion: String
  def scommonsClientVersion: String

  override def definition: Project = {
    super.definition
      .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
      .settings(CommonClientModule.settings: _*)
      .settings(
        coverageExcludedPackages := ".*Css",

        scalaJSUseMainModuleInitializer := true,
        webpackBundlingMode := BundlingMode.LibraryOnly(),
        
        //dev
        fastOptJS / webpackConfigFile := Some(baseDirectory.value / "client.webpack.config.js"),
        //production
        fullOptJS / webpackConfigFile := Some(baseDirectory.value / "client.webpack.config.js"),
        //reload workflow and tests
        scommonsRequireWebpackInTest := true,
        Test / webpackConfigFile := Some(baseDirectory.value / "test.webpack.config.js")
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-client", "scommons-client-ui", None),
    ("scommons-react", "scommons-react-core", None),
    ("scommons-react", "scommons-react-dom", None),
    ("scommons-react", "scommons-react-redux", None),
    
    ("scommons-nodejs", "scommons-nodejs-test", Some("test")),
    ("scommons-react", "scommons-react-test", Some("test"))
  )

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.client" %%% "scommons-client-ui" % scommonsClientVersion
  ))

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.nodejs" %%% "scommons-nodejs-test" % scommonsNodejsVersion,
    "org.scommons.react" %%% "scommons-react-test" % scommonsReactVersion
  ).map(_  % "test"))
}

object CommonClientModule {

  val settings: Seq[Setting[_]] = Seq(
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
        .withSourceMap(false)
        .withESFeatures(_.withUseECMAScript2015(false))
    },
    Test / requireJsDomEnv := false,
    webpack / version := "4.29.0",
    webpackEmitSourceMaps := false,
    Test / parallelExecution := false,

    // required for node.js >= v12.12.0
    // see:
    //   https://github.com/nodejs/node/pull/29919
    Test / scalaJSLinkerConfig ~= {
      _.withSourceMap(true)
    },
    Test / jsEnv := new NodeJSEnv(NodeJSEnv.Config().withArgs(List("--enable-source-maps"))),

    ideExcludedDirectories ++= {
      val base = baseDirectory.value
      List(
        base / "build",
        base / "node_modules"
      )
    }
  )
}
