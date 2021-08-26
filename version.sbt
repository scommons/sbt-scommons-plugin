version in ThisBuild := sys.env.getOrElse("version", default = "0.6.0-SNAPSHOT").stripPrefix("v")
