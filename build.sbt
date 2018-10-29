import Dependencies._
import sbtassembly.MergeStrategy

name := "angular-solution"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  runtime,
  codegen,
  scalaXml,
  slf4jApi % Test,
  slf4jSimple % Test,
  scalaTest % Test,
  scalaTesplusPlay % Test
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.rename
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.github.apuex.angularsolution.codegen.Main")
assemblyJarName in assembly := s"${name.value}.jar"

publishTo := sonatypePublishTo.value
