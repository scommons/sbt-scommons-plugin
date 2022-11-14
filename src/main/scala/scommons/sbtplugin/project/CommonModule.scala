package scommons.sbtplugin.project

import sbt.Keys._
import sbt._
import scommons.sbtplugin.mecha.MechaProjectBuild

trait CommonModule extends ProjectDef with MechaProjectBuild {

  override def definition: Project = Project(id = id, base = base)
    .dependsOn(internalDependencies: _*)
    .settings(CommonModule.settings: _*)
    .settings(
      libraryDependencies ++= (runtimeDependencies.value ++ testDependencies.value)
    )
    .settings(Seq(
      libraryDependencies --= excludeSuperRepoDependencies.value
    ))
    .dependsOnSuperRepo

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Nil
}

object CommonModule {

  // got from here:
  // https://github.com/JetBrains/sbt-ide-settings
  //
  val ideExcludedDirectories = SettingKey[Seq[File]]("ide-excluded-directories")
    .withRank(KeyRanks.Invisible)
  
  val coverageMinimum = SettingKey[Double]("coverage-minimum")
  val coverageExcludedPackages = SettingKey[String]("coverage-excluded-packages")
  val coverallsFailBuildOnError = SettingKey[Boolean](
    "coverallsFailBuildOnError",
    "fail build if coveralls step fails"
  )

  val settings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.13.8",
    scalacOptions ++= Seq(
      //see https://docs.scala-lang.org/overviews/compiler-options/index.html#Warning_Settings
      //"-Xcheckinit",
      "-Xfatal-warnings",
      "-Xlint:_",
      "-Ywarn-macros:after", // Only inspect expanded trees when generating unused symbol warnings
      "-explaintypes",
      "-unchecked",
      "-deprecation",
      "-feature"
    ),

    ideExcludedDirectories := {
      val base = baseDirectory.value
      List(
        base / ".idea",
        base / "target"
      )
    },
    
    coverageMinimum := 80,
    coverallsFailBuildOnError := true,

    //improving performance by disabling this feature that was introduced in:
    //  https://github.com/scoverage/sbt-scoverage/releases/tag/v1.8.0
    Compile / compile / scalacOptions -= "-P:scoverage:reportTestName",

    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  )
}
