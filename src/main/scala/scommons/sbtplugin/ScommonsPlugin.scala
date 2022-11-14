package scommons.sbtplugin

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.util.{BundlesUtils, ResourcesUtils}
import scalajsbundler.{NpmPackage, Webpack}
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object ScommonsPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = ScalaJSBundlerPlugin

  object autoImport {
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
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    
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
      "org.scommons.client" % "scommons-client-ui" % "*"
    ),

    scommonsBundlesFileFilter := NothingFilter,

    scommonsNodeJsTestLibs := Nil,

    scommonsRequireWebpackInTest := false,
    
    sjsStageSettings(fastOptJS, Compile),
    sjsStageSettings(fullOptJS, Compile),
    sjsStageSettings(fastOptJS, Test),
    sjsStageSettings(fullOptJS, Test),

    Test / fastOptJS := {
      val logger = streams.value.log
      val testLibs = scommonsNodeJsTestLibs.value
      val sjsOutput = (Test / fastOptJS).value
      val targetDir = sjsOutput.data.getParentFile
      val bundleOutput =
        if (testLibs.nonEmpty) {
          val sjsOutputName = sjsOutput.data.name.stripSuffix(".js")
          val bundle = new File(targetDir, s"$sjsOutputName-bundle.js")
  
          logger.info(s"Writing NodeJs test bundle\n\t$bundle")
          IO.delete(bundle)
          testLibs.foreach { jsFile =>
            IO.write(bundle, IO.read(new File(targetDir, jsFile)), append = true)
          }
          IO.write(bundle, IO.read(sjsOutput.data), append = true)
  
          Attributed(bundle)(sjsOutput.metadata)
        }
        else sjsOutput

      if (scommonsRequireWebpackInTest.value) {
        val customWebpackConfigFile = (Test / webpackConfigFile).value
        val nodeArgs = (Test / webpackNodeArgs).value
        val bundleName = bundleOutput.data.name.stripSuffix(".js")
        val webpackOutput = targetDir / s"$bundleName-webpack-out.js"
        val webpackVersion = (webpack / version).value

        logger.info("Executing webpack...")
        val loader = bundleOutput.data

        val configArgs = customWebpackConfigFile match {
          case Some(configFile) =>
            val customConfigFileCopy = Webpack.copyCustomWebpackConfigFiles(targetDir, webpackResources.value.get)(configFile)
            Seq("--config", customConfigFileCopy.getAbsolutePath)
          case None =>
            Seq.empty
        }

        val allArgs = Seq(
          "--entry", loader.absolutePath,
          "--output-path", targetDir.absolutePath,
          "--output-filename", webpackOutput.name
        ) ++ configArgs

        NpmPackage(webpackVersion).major match {
          case Some(5) =>
            Webpack.run(nodeArgs: _*)(allArgs: _*)(targetDir, logger)
          case Some(x) =>
            sys.error(s"Unsupported webpack major version $x")
          case None =>
            sys.error("No webpack version defined")
        }
        Attributed(webpackOutput)(bundleOutput.metadata)
      }
      else bundleOutput
    },

    // revert the change for clean task: https://github.com/sbt/sbt/pull/3834/files#r172686677
    // to keep the logic for cleanKeepFiles and avoid the error:
    //   cleanKeepFiles contains directory/file that are not directly under cleanFiles
    clean := doClean(Seq(managedDirectory.value, target.value), cleanKeepFiles.value),

    cleanKeepFiles ++= {
      val scalaVer = scalaBinaryVersion.value
      Seq(
        target.value / s"scala-$scalaVer" / "scalajs-bundler" / "main" / "node_modules",
        target.value / s"scala-$scalaVer" / "scalajs-bundler" / "test" / "node_modules",
        target.value / "scalajs-bundler-jsdom" / "node_modules"
      )
    }
  )

  private def sjsStageSettings(sjsStage: TaskKey[Attributed[File]], config: ConfigKey) = {
    config / sjsStage := {
      copyWebpackResources(
        streams.value.log,
        (config / sjsStage / crossTarget).value,
        (config / fullClasspath).value,
        scommonsResourcesFileFilter.value,
        scommonsResourcesArtifacts.value
      )
      genWebpackBundles(
        streams.value.log,
        (config / sjsStage / crossTarget).value,
        (config / fullClasspath).value,
        scommonsBundlesFileFilter.value
      )
      (config / sjsStage).value
    }
  }

  private def copyWebpackResources(log: Logger,
                                   webpackDir: File,
                                   cp: Seq[Attributed[File]],
                                   fileFilter: FileFilter,
                                   includeArtifacts: Seq[ModuleID]): Unit = {

    ResourcesUtils.extractFromClasspath(msg => log.info(msg), webpackDir, cp, fileFilter, includeArtifacts)
  }

  private def genWebpackBundles(log: Logger,
                                webpackDir: File,
                                cp: Seq[Attributed[File]],
                                fileFilter: FileFilter): Unit = {

    if (fileFilter != NothingFilter) {
      BundlesUtils.genFromClasspath(msg => log.info(msg), webpackDir, cp, fileFilter)
    }
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
