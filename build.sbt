
val projectName                     = "gatlinggen"
val companyName                     = "MaxMind"
val projectVersion                  = "0.9-SNAPSHOT"
val projectPackage                  = s"com.${ companyName.toLowerCase }.$projectName"
val simLauncher                     = Some(s"$projectPackage.simulation.gensim.GenSimLauncherApp")
val opts                            = Seq("-Yrangepos", "-feature", "-unchecked", "-deprecation", "-encoding", "utf8")
val scalaSdkVersion                 = "2.11.7"
val akkaV                           = "2.3.9"
val sprayV                          = "1.3.3"
val gatlingV                        = "2.1.7"

name                               := projectName
organization                       := companyName
version                            := projectVersion
scalaVersion                       := scalaSdkVersion
autoScalaLibrary                   := true
ivyLoggingLevel                    := UpdateLogging.Full
logBuffered                        := true

mainClass in (Compile, run      )  := simLauncher
mainClass in (Compile, packageBin) := simLauncher
mainClass in assembly              := simLauncher
assemblyJarName in assembly        := s"$projectName.jar"
resolvers                          += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"
resolvers                          += "Sonatype OSS Releases"  at "https://oss.sonatype.org/content/repositories/snapshots"
scalacOptions                      ++= opts
libraryDependencies                += "com.github.alexarchambault" %% "case-app"                  % "1.0.0-SNAPSHOT"                    withSources() withJavadoc()
libraryDependencies                += "com.lihaoyi"                %  "ammonite-repl"             % "0.5.0" cross CrossVersion.full     withSources() withJavadoc()
libraryDependencies                += "com.lihaoyi"                %% "ammonite-ops"              % "0.5.0"                             withSources() withJavadoc()
libraryDependencies                += "com.squants"                %% "squants"                   % "0.6.1-SNAPSHOT"                    withSources() withJavadoc()
libraryDependencies                += "com.storm-enroute"          %% "scalameter"                % "0.7"                               withSources() withJavadoc()
libraryDependencies                += "com.typesafe.akka"          %% "akka-actor"                % akkaV                               withSources() withJavadoc()
libraryDependencies                += "io.gatling.highcharts"      %  "gatling-charts-highcharts" % "2.1.7"                             withSources() withJavadoc()
libraryDependencies                += "org.asynchttpclient"        % "async-http-client"          % "2.0.0-alpha21"                     withSources() withJavadoc()
libraryDependencies                += "io.spray"                   %% "spray-can"                 % sprayV                              withSources() withJavadoc()
libraryDependencies                += "io.spray"                   %% "spray-routing-shapeless2"  % sprayV                              withSources() withJavadoc()
libraryDependencies                += "io.spray"                   %% "spray-json"                % "1.3.2"                             withSources() withJavadoc()
libraryDependencies                += "org.scalacheck"             %% "scalacheck"                % "1.12.5"                            withSources() withJavadoc()
libraryDependencies                += "org.scalaz"                 %% "scalaz-core"               % "7.1.5"                             withSources() withJavadoc()
libraryDependencies                += "com.github.melrief"         %% "pureconfig"                % "0.1.4"                             withSources() withJavadoc()

libraryDependencies                += "org.specs2"                 %% "specs2-core"               % "3.6.5"                             withSources() withJavadoc()
libraryDependencies                += "org.specs2"                 %% "specs2-scalacheck"         % "3.6.5"                             withSources() withJavadoc()
libraryDependencies                += "org.specs2"                 %% "specs2-html"               % "3.6.5"                             withSources() withJavadoc()
libraryDependencies                += "org.specs2"                 %% "specs2-matcher-extra"      % "3.6.5"                             withSources() withJavadoc()

scalacOptions in Test              ++= opts
test in assembly                   := {}
publishArtifact in Test            := true
parallelExecution in Test          := false
testOptions in Test                += Tests.Argument(TestFrameworks.Specs2, "html", "console")
logBuffered in Test                := true

assemblyMergeStrategy in assembly := {
    case PathList("org", "scalatools", xs @ _*)                           => MergeStrategy.first
    case PathList("org", "threeten", "bp", xs @ _*)                       => MergeStrategy.last
    case PathList(xs @ _*) if xs contains "derive"                        => MergeStrategy.first
    case PathList(xs @ _*) if xs contains "io.netty.versions.properties"  => MergeStrategy.filterDistinctLines
    case x                                                                => val oldStrategy = (assemblyMergeStrategy in assembly).value
                                                                             oldStrategy(x)
}
