package scommons.sbtplugin.project

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.linker.interface.ESVersion
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories

trait CommonNodeJsModule extends CommonModule {

  def scommonsNodejsVersion: String

  override def definition: Project = {
    super.definition
      .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
      .settings(CommonNodeJsModule.settings: _*)
  }

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-nodejs", "scommons-nodejs-core", None),

    ("scommons-nodejs", "scommons-nodejs-test", Some("test"))
  )

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.nodejs" %%% "scommons-nodejs-core" % scommonsNodejsVersion
  ))

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.nodejs" %%% "scommons-nodejs-test" % scommonsNodejsVersion
  ).map(_ % "test"))
}

object CommonNodeJsModule {

  val settings: Seq[Setting[_]] = Seq(
    scalacOptions ++= Seq(
      //see:
      //  http://www.scala-js.org/news/2021/12/10/announcing-scalajs-1.8.0/
      "-P:scalajs:nowarnGlobalExecutionContext"
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
        .withSourceMap(false)
        .withESFeatures(_.withESVersion(ESVersion.ES5_1))
    },
    Test / requireJsDomEnv := false,
    webpack / version := "5.74.0",
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
