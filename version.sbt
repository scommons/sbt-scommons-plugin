ThisBuild / version := sys.env.getOrElse("version", default = "0.9.0-SNAPSHOT").stripPrefix("v")
