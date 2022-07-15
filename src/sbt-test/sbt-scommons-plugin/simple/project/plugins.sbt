
resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"

//addSbtPlugin("org.scommons.sbt" % "sbt-scommons-plugin" % "0.1.0-SNAPSHOT")
sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("org.scommons.sbt" % "sbt-scommons-plugin" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.8")

addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.20.0")
