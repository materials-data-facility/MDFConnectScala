
import sbt.{TestFrameworks, Tests}

name := "MDFConnect"

version := "0.1"

scalaVersion := "2.10.6"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// https://mvnrepository.com/artifact/com.typesafe.play/play
libraryDependencies += "com.typesafe.play" %% "play" % "2.2.1"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.10.0" % Test

libraryDependencies += "org.specs2" %% "specs2-mock" % "3.10.0" % Test

libraryDependencies += "org.mockito" % "mockito-inline" % "2.7.22" % Test

//coverageExcludedPackages := "<empty>;Reverse.*;controllers.javascript.*;models\\.data\\..*;models.daos.*;modules.*;router.*; utils.auth.*;utils.Filters; views.*;services.adaptors.*"

scalacOptions in Test ++= Seq("-Yrangepos")
