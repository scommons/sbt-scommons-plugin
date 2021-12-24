version in ThisBuild := sys.env.getOrElse("version", default = "0.8.0-SNAPSHOT").stripPrefix("v")
