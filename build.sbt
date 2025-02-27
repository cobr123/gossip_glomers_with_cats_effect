ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

ThisBuild / scalacOptions += "-Wnonunit-statement"
ThisBuild / scalacOptions += "-no-indent"

lazy val root = (project in file("."))
  .settings(
    name := "gossip_glomers_with_cats_effect",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-parser" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "co.fs2" %% "fs2-core" % "3.11.0",
      "co.fs2" %% "fs2-io" % "3.11.0"
    )
  )

lazy val echo = (project in file("echo"))
  .dependsOn(root)
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "echo",
    Compile / mainClass := Some("com.example.Main"),
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--install-exit-handlers",
      "-H:IncludeResources=.*",
    ) ++ Option(nativeImageAgentOutputDir.value)
      .filter(_.exists())
      .map(file => s"-H:ReflectionConfigurationFiles=${(file / "reflect-config.json").absolutePath}")
      .toSeq,
    nativeImageVersion := "22.3.0" // It should be at least version 21.0.0
  )

lazy val `unique-ids` = (project in file("unique-ids"))
  .dependsOn(root)
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "unique-ids",
    Compile / mainClass := Some("com.example.Main"),
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--install-exit-handlers",
      "-H:IncludeResources=.*",
    ) ++ Option(nativeImageAgentOutputDir.value)
      .filter(_.exists())
      .map(file => s"-H:ReflectionConfigurationFiles=${(file / "reflect-config.json").absolutePath}")
      .toSeq,
    nativeImageVersion := "22.3.0" // It should be at least version 21.0.0
  )

lazy val broadcast = (project in file("broadcast"))
  .dependsOn(root)
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "broadcast",
    Compile / mainClass := Some("com.example.Main"),
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--install-exit-handlers",
      "-H:IncludeResources=.*",
    ) ++ Option(nativeImageAgentOutputDir.value)
      .filter(_.exists())
      .map(file => s"-H:ReflectionConfigurationFiles=${(file / "reflect-config.json").absolutePath}")
      .toSeq,
    nativeImageVersion := "22.3.0" // It should be at least version 21.0.0
  )
