import Dependencies._

lazy val baseSettings = Seq(
  name := "plentymarkets-rest-api-client",
  version := "0.1",
  scalaVersion := "2.13.7",
  idePackagePrefix.withRank(KeyRanks.Invisible) := Some("org.hardsoft321.plentymarkets")
)

lazy val rootProject = (project in file("."))
  .settings(
    baseSettings,
    libraryDependencies ++= rootDependencies
  )
