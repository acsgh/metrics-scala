ThisBuild / organization := "com.github.acsgh.metrics.scala"
ThisBuild / scalaVersion := "2.13.1"

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

lazy val commonSettings = Seq(
  scalacOptions += "-feature",
  scalacOptions += "-deprecation",
  sonatypeProfileName := "com.github.acsgh",
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  crossScalaVersions := List("2.12.10", "2.13.1"),
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  libraryDependencies ++= Seq(
    "com.beachape" %% "enumeratum" % "1.5.13",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "org.slf4j" % "slf4j-api" % "1.7.21",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    "org.scalacheck" %% "scalacheck" % "1.14.2" % Test,
    "org.pegdown" % "pegdown" % "1.4.2" % Test,
    "org.scalamock" %% "scalamock" % "4.4.0" % Test,
  ),
  homepage := Some(url("https://github.com/acsgh/metrics-scala")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := sonatypePublishToBundle.value,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/acsgh/metrics-scala"),
      "scm:git:git@github.com:acsgh/metrics-scala.git"
    )
  ),
  developers := List(
    Developer("acsgh", "Alberto Crespo", "albertocresposanchez@gmail.com", url("https://github.com/acsgh"))
  )
)


lazy val root = (project in file("."))
  .settings(
    name := "metrics-scala",
    commonSettings,
    crossScalaVersions := Nil,
    publish / skip := true
  )
  .aggregate(
    core,
    influxDb
  )

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.github.acsgh.common.scala" %% "core" % "1.2.15",
    )
  )

lazy val influxDb = (project in file("influx-db"))
  .settings(
    organization := "com.github.acsgh.metrics.scala.publisher",
    name := "influx-db",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.9"
    )
  )
  .dependsOn(core)
