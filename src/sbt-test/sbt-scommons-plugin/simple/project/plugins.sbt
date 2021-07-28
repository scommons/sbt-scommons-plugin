
resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"

val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("0.6.31")

//addSbtPlugin("org.scommons.sbt" % "sbt-scommons-plugin" % "0.1.0-SNAPSHOT")
sys.props.get("plugin.version") match {
  case Some(x) =>
    if (scalaJSVersion.startsWith("0.6")) {
      addSbtPlugin("org.scommons.sbt" % "sbt-scommons-plugin-sjs06" % x)
    }
    else {
      addSbtPlugin("org.scommons.sbt" % "sbt-scommons-plugin" % x)
    }
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.8")

if (scalaJSVersion.startsWith("0.6")) {
  addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler-sjs06" % "0.18.0")
}
else {
  addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.18.0")
}
