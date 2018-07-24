
import sbt.{TestFrameworks, Tests}

name := "MDFConnectScala"

organization := "org.materialsdatafacility.mdfconnect"

description := "Scala bindings for MDF Connect Service"

version := "1.0"

scalaVersion := "2.10.6"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// https://mvnrepository.com/artifact/com.typesafe.play/play
libraryDependencies += "com.typesafe.play" %% "play" % "2.2.1"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.10.0" % Test

libraryDependencies += "org.specs2" %% "specs2-mock" % "3.10.0" % Test

libraryDependencies += "org.mockito" % "mockito-inline" % "2.7.22" % Test

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "content/repositories/releases")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

pomIncludeRepository := { _ => false }

licenses := Seq("Apache" -> url("https://opensource.org/licenses/Apache-2.0"))

homepage := Some(url("https://materialsdatafacility.org"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/materials-data-facility/MDFConnectScala"),
    "scm:git@github.com:materials-data-facility/MDFConnectScala.git"
  )
)

scalacOptions in Test ++= Seq("-Yrangepos")
