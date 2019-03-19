package scommons.sbtplugin

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.util.ResourcesUtils

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin

object ScommonsPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires = ScalaJSBundlerPlugin

  object autoImport {
    val scommonsResourcesFileFilter: SettingKey[FileFilter] = settingKey[FileFilter](
      "File filter of resources files, that should be automatically copied/extracted to the webpack directory"
    )
    val scommonsResourcesArtifacts: SettingKey[Seq[ModuleID]] = settingKey[Seq[ModuleID]](
      "List of artifacts (JARs) with resources, that should be automatically extracted to the webpack directory"
    )
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    scommonsResourcesFileFilter :=
      "*.js" ||
        "*.css" ||
        "*.ico" ||
        "*.png" ||
        "*.jpg" ||
        "*.jpeg" ||
        "*.gif",
    scommonsResourcesArtifacts := Seq(
      "org.scommons.react" % "scommons-react-core" % "*",
      "org.scommons.client" % "scommons-client-ui" % "*"
    ),

    sjsStageSettings(fastOptJS, Compile),
    sjsStageSettings(fullOptJS, Compile),
    sjsStageSettings(fastOptJS, Test),
    sjsStageSettings(fullOptJS, Test),

    // revert the change for clean task: https://github.com/sbt/sbt/pull/3834/files#r172686677
    // to keep the logic for cleanKeepFiles and avoid the error:
    //   cleanKeepFiles contains directory/file that are not directly under cleanFiles
    clean := doClean(Seq(managedDirectory.value, target.value), cleanKeepFiles.value),

    cleanKeepFiles ++= Seq(
      target.value / "scala-2.12" / "scalajs-bundler" / "main" / "node_modules",
      target.value / "scala-2.12" / "scalajs-bundler" / "test" / "node_modules",
      target.value / "scalajs-bundler-jsdom" / "node_modules"
    )
  )

  private def sjsStageSettings(sjsStage: TaskKey[Attributed[File]], config: ConfigKey) = {
    sjsStage in config := {
      copyWebpackResources(
        streams.value.log,
        (crossTarget in (config, sjsStage)).value,
        (fullClasspath in config).value,
        scommonsResourcesFileFilter.value,
        scommonsResourcesArtifacts.value
      )
      (sjsStage in config).value
    }
  }

  private def copyWebpackResources(log: Logger,
                                   webpackDir: File,
                                   cp: Seq[Attributed[File]],
                                   fileFilter: FileFilter,
                                   includeArtifacts: Seq[ModuleID]): Unit = {

    ResourcesUtils.extractFromClasspath(msg => log.info(msg), webpackDir, cp, fileFilter, includeArtifacts)
  }

  private def doClean(clean: Seq[File], preserve: Seq[File]): Unit =
    IO.withTemporaryDirectory { temp =>
      val (dirs, files) = preserve.filter(_.exists).flatMap(_.allPaths.get).partition(_.isDirectory)
      val mappings = files.zipWithIndex map { case (f, i) => (f, new File(temp, i.toHexString)) }
      IO.move(mappings)
      IO.delete(clean)
      IO.createDirectories(dirs) // recreate empty directories
      IO.move(mappings.map(_.swap))
    }
}
