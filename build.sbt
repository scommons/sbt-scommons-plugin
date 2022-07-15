
val ideExcludedDirectories = SettingKey[Seq[File]]("ide-excluded-directories")
  .withRank(KeyRanks.Invisible)

lazy val `sbt-scommons-plugin` = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .settings(
    sbtPlugin := true,
    organization := "org.scommons.sbt",
    name := "sbt-scommons-plugin",
    description := "Sbt auto-plugin with common Scala/Scala.js tasks/utils",
    scalacOptions ++= Seq(
      //"-Xcheckinit",
      "-Xfatal-warnings",
      "-feature",
      "-deprecation",
      "-encoding", "UTF-8",
      "-unchecked",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture"
    ),
    
    ideExcludedDirectories := {
      val base = baseDirectory.value
      List(
        base / ".idea",
        base / "target"
      )
    },
    
    coverageMinimum := 80,
    coverageHighlighting := false,
    coverageExcludedPackages := ".*mecha.*;.*project.*",

    addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0"),
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.5.1"),
    addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0"),

    //addSbtPlugin("com.storm-enroute" % "mecha" % "0.3"), //TODO: use version for sbt 1.x
    
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
      "org.scalamock" %% "scalamock" % "5.1.0" % "test"
    ),

    //resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",

    //
    // publish/release related settings:
    //
    sonatypeProfileName := "org.scommons",
    publishMavenStyle := true,
    Test / publishArtifact := false,
    publishTo := sonatypePublishToBundle.value,
    pomExtra := {
      <url>https://github.com/scommons/sbt-scommons-plugin</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:scommons/sbt-scommons-plugin.git</url>
          <connection>scm:git@github.com:scommons/sbt-scommons-plugin.git</connection>
        </scm>
        <developers>
          <developer>
            <id>viktorp</id>
            <name>Viktor Podzigun</name>
            <url>https://github.com/viktor-podzigun</url>
          </developer>
        </developers>
    },
    pomIncludeRepository := {
      _ => false
    }
  )
