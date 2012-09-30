name := "Attempt"

scalaVersion := "2.9.2"

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"

// Repositories in which to find dependencies
resolvers ++= Seq(
    "Specs Repository" at "http://oss.sonatype.org/content/repositories/releases"
)

// Application dependencies
libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.12.1" % "test"
)
