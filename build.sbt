import sbt.Keys.libraryDependencies
import sbt.url

name := "locks"

version := "0.1"
val circeV = "0.10.0"
val scalaV = "2.12.6"
val slf4jV = "1.7.25"
val logbackV = "1.2.3"
val finchV = "0.25.0"
val sangriaVersion = "1.4.2"
val sangriaCirceVersion = "1.2.1"
cancelable in Global := true

scalacOptions += "-P:scalajs:sjsDefinedByDefault"

lazy val common = Seq(name := "locks-common",
  organization := "org.zardina",
  scalaVersion := scalaV,
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.4" % Test,
  libraryDependencies += "org.scalamock" %%% "scalamock" % "4.1.0" % Test)

lazy val core = crossProject.in(file("core"))
  .settings(common)
  .settings(Seq(
    name := "locks-core",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeV)
  ))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val slick = project.in(file("slick")).settings(common).settings(
  name := "locks-slick",
  libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.3",
  libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34",
  libraryDependencies += "org.slf4j" % "slf4j-api" % slf4jV,
  libraryDependencies += "com.h2database" % "h2" % "1.4.196",
  libraryDependencies += "org.liquibase" % "liquibase-core" % "3.6.1"
).dependsOn(coreJVM)

lazy val server = crossProject.in(file("server")).settings(common).settings(
  name := "locks-server"
)


lazy val serverJVM = server.jvm.settings(
  libraryDependencies ++= Seq(
    "com.github.finagle" %% "finch-core" % finchV,
    "com.github.finagle" %% "finch-circe" % finchV,
    "org.sangria-graphql" %% "sangria" % sangriaVersion,
    "org.sangria-graphql" %% "sangria-relay" % sangriaVersion,
    "org.sangria-graphql" %% "sangria-circe" % sangriaCirceVersion,
    "ch.qos.logback" % "logback-classic" % logbackV,
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.9.0.201710071750-r"
  ),
  (updateUi in Compile) := {
    val jsFile = (fastOptJS in Compile in serverJS).value.data.getAbsoluteFile
    val classDir = (classDirectory in Compile).value
    IO.copyFile(jsFile, classDir / jsFile.getName)
  }
).dependsOn(coreJVM, slick)

val updateUi = taskKey[Unit]("copy ui resources to class dir")

lazy val serverJS = server.js.settings(
  scalaJSUseMainModuleInitializer := true,
  artifactPath in (Compile, fastOptJS) := target.value / "locks-ui.js",
  artifactPath in (Compile, fullOptJS) := (artifactPath in (Compile, fastOptJS)).value,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    "org.querki" %%% "jquery-facade" % "1.2",
    "com.flowtick" %%% "pages" % "0.1.6",
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    "com.thoughtworks.binding" %%% "futurebinding" % "latest.release"
  ),
  skip in packageJSDependencies := false,
  jsDependencies += "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
).dependsOn(coreJS)