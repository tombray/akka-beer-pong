name := """akka-beer-pong"""

version := "1.0"

scalaVersion := "2.11.5"

val akka = "2.3.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka"          %%  "akka-actor"                            % akka,
  "com.typesafe.akka"          %%  "akka-cluster"                           % akka,
  "com.typesafe.akka"          %%  "akka-remote"                            % akka,
  "com.typesafe.akka"          %%  "akka-contrib"                           % akka,
  "com.typesafe.akka"          %%  "akka-slf4j"                            % akka,
  "com.typesafe.akka"          %%  "akka-multi-node-testkit"               % akka,
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")
