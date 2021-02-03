import Dependencies._

ThisBuild / scalaVersion := "3.0.0-M3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.github.novakov-alexey"
ThisBuild / organizationName := "novakov-alexey"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    name := "ann",
    libraryDependencies ++=
      Seq(scalaTest % Test)
  )
//  .enablePlugins(ScalaNativePlugin)

//import scala.scalanative.build._
//nativeConfig ~= {
//  _.withLTO(LTO.thin)
//    .withMode(Mode.releaseFull)
//    .withGC(GC.immix)
//}

scalacOptions ++= {
  if (isDotty.value)
    Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
//      "-indent",
//      "-rewrite"
      // "-new-syntax",
      // "-Xfatal-warnings" will be added after the migration
    )
  else
    Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-deprecation",
      "-language:implicitConversions",
      "-Xfatal-warnings",
      "-Wunused:imports,privates,locals",
      "-Wvalue-discard"
    )
}
