val DoobieVersion              = "0.5.3"
val ScalaTestVersion           = "3.0.5"
val TestContainersScalaVersion = "0.17.0"
val TestContainersPSQLVersion  = "1.7.2"
val FlyWayVersion              = "5.2.1"
val MagnoliaVersion            = "0.10.0"
val AdversariaVersion          = "0.2.0"
val InflectorVersion           = "1.2.2"

val joint = (project in file("."))
  .settings(
    name := "joint",
    organization := "machinespir.it",
    version := "0.1",
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      "org.tpolecat"       %% "doobie-core"          % DoobieVersion,
      "org.tpolecat"       %% "doobie-hikari"        % DoobieVersion,
      "org.tpolecat"       %% "doobie-postgres"      % DoobieVersion,
      "org.tpolecat"       %% "doobie-scalatest"     % DoobieVersion % Test,
      "com.dimafeng"       %% "testcontainers-scala" % TestContainersScalaVersion % Test,
      "org.testcontainers" % "postgresql"            % TestContainersPSQLVersion % Test,
      "org.flywaydb"       % "flyway-core"           % FlyWayVersion % Test,
      "com.propensive"     %% "magnolia"             % MagnoliaVersion,
      "com.propensive"     %% "adversaria"           % AdversariaVersion,
      "org.atteo"          % "evo-inflector"         % InflectorVersion
    )
  )
