name := "Attempt"

organization := "com.roundeights"

version := "0.1"

scalaVersion := "2.10.0"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature")

// Application dependencies
libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.13" % "test"
)

