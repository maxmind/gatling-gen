
name             := "gatlinggen"
organization     := "MaxMind"
version          := "0.1-SNAPSHOT"
scalaVersion     := "2.11.7"
autoScalaLibrary := true

mainClass in (Compile, run      )  := Some("com.maxmind.gatling.dev.SimulationLauncherApp")
mainClass in (Compile, packageBin) := Some("com.maxmind.gatling.dev.SimulationLauncherApp")
mainClass in assembly              := Some("com.maxmind.gatling.dev.SimulationLauncherApp")

assemblyJarName in assembly := "gatlinggen.jar"
test in assembly := {}

assemblyMergeStrategy in assembly := {
    case PathList("org", "scalatools", xs @ _*) => MergeStrategy.first
    case PathList(ys @ _*) if ys contains "derive" => MergeStrategy.first
    case PathList("org", "threeten", "bp", zs @ _*) => MergeStrategy.discard
    case x => val oldStrategy = (assemblyMergeStrategy in assembly).value
              oldStrategy(x)
}

val akkaV    = "2.3.9"
val sprayV   = "1.3.3"
val gatlingV = "2.1.7"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"
resolvers += "Sonatype OSS Releases"  at "https://oss.sonatype.org/content/repositories/snapshots"


libraryDependencies += "org.specs2"                 %% "specs2-core"               % "3.6.5"                % "test"     withSources() withJavadoc()
libraryDependencies += "org.specs2"                 %% "specs2-scalacheck"         % "3.6.5"                % "test"     withSources() withJavadoc()
libraryDependencies += "org.specs2"                 %% "specs2-matcher-extra"      % "3.6.5"                % "test"     withSources() withJavadoc()

libraryDependencies += "com.github.alexarchambault" %% "case-app"                  % "1.0.0-SNAPSHOT"                    withSources() withJavadoc()
libraryDependencies += "com.lihaoyi"                %  "ammonite-repl"             % "0.5.0" cross CrossVersion.full     withSources() withJavadoc()
libraryDependencies += "com.lihaoyi"                %% "ammonite-ops"              % "0.5.0"                             withSources() withJavadoc()
libraryDependencies += "com.squants"                %% "squants"                   % "0.6.1-SNAPSHOT"                    withSources() withJavadoc()
libraryDependencies += "com.storm-enroute"          %% "scalameter"                % "0.7"                               withSources() withJavadoc()
libraryDependencies += "com.typesafe.akka"          %% "akka-actor"                % akkaV                               withSources() withJavadoc()
libraryDependencies += "io.gatling.highcharts"      %  "gatling-charts-highcharts" % "2.1.7"                             withSources() withJavadoc()
libraryDependencies += "io.spray"                   %% "spray-can"                 % sprayV                              withSources() withJavadoc()
libraryDependencies += "io.spray"                   %% "spray-routing-shapeless2"  % sprayV                              withSources() withJavadoc()
libraryDependencies += "io.spray"                   %% "spray-json"                % "1.3.2"                             withSources() withJavadoc()
libraryDependencies += "org.scalacheck"             %% "scalacheck"                % "1.12.5"                            withSources() withJavadoc()
libraryDependencies += "org.scalaz"                 %% "scalaz-core"               % "7.1.5"                             withSources() withJavadoc()


val opts = Seq("-Yrangepos", "-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions         ++= opts
scalacOptions in Test ++= opts


parallelExecution in Test := true
logBuffered in Test := true
logBuffered := true
