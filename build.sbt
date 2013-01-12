name := "Attempt"

scalaVersion := "2.10.0"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature")

// Repositories in which to find dependencies
resolvers ++= Seq(
    "Specs Repository" at "http://oss.sonatype.org/content/repositories/releases"
)

// Application dependencies
libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.13" % "test"
)
