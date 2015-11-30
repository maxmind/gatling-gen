

name             := "gatlinggen"
organization     := "MaxMind"
version          := "0.1-SNAPSHOT"
scalaVersion     := "2.11.7"
autoScalaLibrary := true

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.lihaoyi"           %  "ammonite-repl"            % "0.4.9" cross CrossVersion.full     withSources() withJavadoc()
libraryDependencies += "com.lihaoyi"           %% "ammonite-ops"             % "0.5.0"                             withSources() withJavadoc()
libraryDependencies += "com.squants"           %% "squants"                  % "0.6.1-SNAPSHOT"                    withSources() withJavadoc()
libraryDependencies += "com.storm-enroute"     %% "scalameter"               % "0.7"                               withSources() withJavadoc()
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7"                             withSources() withJavadoc()
libraryDependencies += "org.scalacheck"        %% "scalacheck"               % "1.12.5"                            withSources() withJavadoc()


libraryDependencies += "org.scalaz"            %% "scalaz-core"              % "7.1.5"                             withSources() withJavadoc()
libraryDependencies += "org.specs2"            %% "specs2-core"              % "3.6.5"                % "test"     withSources() withJavadoc()
libraryDependencies += "org.specs2"            %% "specs2-scalacheck"        % "3.6.5"                % "test"     withSources() withJavadoc()
libraryDependencies += "org.specs2"            %% "specs2-matcher-extra"     % "3.6.5"                % "test"     withSources() withJavadoc()


scalacOptions in Test ++= Seq("-Yrangepos")
parallelExecution in Test := true
logBuffered in Test := true
logBuffered := true
