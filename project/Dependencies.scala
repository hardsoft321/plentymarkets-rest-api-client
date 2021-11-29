import sbt._

object Dependencies {
  /**
   * Versions of dependencies
   */
  val playJsonVersion = "2.9.2"
  val akkaVersion = "2.6.17"
  val akkaHttpVersion = "10.2.7"
  val playWSVersion = "2.8.8"
  val scalaLoggingVersion = "3.9.4"

  /**
   * Root project dependencies list
   */
  val rootDependencies = Seq(
    "com.typesafe.play" %% "play-json" % playJsonVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  )
}
