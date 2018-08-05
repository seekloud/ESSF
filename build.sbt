import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}


lazy val baseSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  organization := "org.seekloud",
  scalacOptions ++= Seq(
    //"-deprecation",
    "-feature"
  ),
  javacOptions ++= Seq("-encoding", "UTF-8"),
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)


lazy val core =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("core"))
    .settings(
      name := "essf",
      baseSettings,
      publishSettings
    )
    .settings(
      libraryDependencies ++= Seq(
        "org.seekloud" %%% "byteobject" % "0.1.1",
      )
    )
    .jvmSettings()
    .jsSettings(
      scalaJSUseMainModuleInitializer := true,
      skip in packageJSDependencies := false,
      inConfig(Compile)(
        Seq(
          fullOptJS,
          fastOptJS,
          packageJSDependencies,
          packageMinifiedJSDependencies
        ).map(f => (crossTarget in f) ~= (_ / "sjsout"))
      )
    )

// Needed, so sbt finds the projects
lazy val coreJVM = core.jvm
lazy val coreJS = core.js




lazy val jvmExample = project.in(file("example/inJVM"))
  .dependsOn(coreJVM)
  .settings(
    name := "jvmExample",
    baseSettings
  )


lazy val jsExample = project.in(file("example/inJS"))
  .dependsOn(coreJS)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "jsExample",
    baseSettings
  )
  .settings(
    scalaJSUseMainModuleInitializer := true,
    skip in packageJSDependencies := false,
    inConfig(Compile)(
      Seq(
        fullOptJS,
        fastOptJS,
        packageJSDependencies,
        packageMinifiedJSDependencies
      ).map(f => (crossTarget in f) ~= (_ / "sjsout"))
    ))




lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  // 本地maven仓库位置
  /*  publishTo := Some(
      Resolver.file("file", new File(Path.userHome.absolutePath + "/repos"))),
  */ pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomExtra :=
  <url>https://github.com/seekloud/essf</url>
    <licenses>
      <license>
        <name>Apache 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/seekloud/essf.git</url>
      <connection>scm:git@github.com:seekloud/essf.git</connection>
    </scm>
    <developers>
      <developer>
        <id>sometao</id>
        <name>Tao Zhang</name>
        <url>https://github.com/sometao</url>
      </developer>
    </developers>
)










