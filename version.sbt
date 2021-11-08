version in ThisBuild := sys.env.getOrElse("version", default = "0.7.0-SNAPSHOT").stripPrefix("v")
