import sbt._

object Dependencies {
  lazy val scalaVersionNumber    = "2.12.6"
  lazy val artifactVersionNumber = "1.0.0"
  lazy val artifactGroupName     = "com.github.apuex.angularsolution"

  lazy val runtime         = "com.github.apuex.springbootsolution" %%  "runtime"                   % "1.0.6"
  lazy val codegen         = "com.github.apuex.springbootsolution" %%  "codegen"                   % "1.0.6"
  lazy val scalaXml        = "org.scala-lang.modules"    %%  "scala-xml"                           % "1.0.6"

  lazy val jodaTime        = "joda-time"                 %   "joda-time"                           % "2.9.9"

  lazy val logback         = "ch.qos.logback"            %   "logback-classic"                     % "1.2.3"
  lazy val slf4jApi        = "org.slf4j"                 %  "slf4j-api"                            % "1.7.25"
  lazy val slf4jSimple     = "org.slf4j"                 %  "slf4j-simple"                         % "1.7.25"
  lazy val scalaTest       = "org.scalatest"             %% "scalatest"                            % "3.0.4"
  lazy val scalaTesplusPlay = "org.scalatestplus.play"    %%  "scalatestplus-play"                  % "3.1.2"
  lazy val scalacheck      = "org.scalacheck"            %%  "scalacheck"                          % "1.13.4"
  lazy val scalaTestPlusPlay = "org.scalatestplus.play"  %%  "scalatestplus-play"                  % "3.1.2"

  lazy val localRepo = Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
}
