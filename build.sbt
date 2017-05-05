organization in ThisBuild := "com.kanban"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val `kanban` = (project in file("."))
  .aggregate(`board-api`, `board-impl`, `task-api`, `task-impl`)

lazy val `task-api` = (project in file("task-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok,
      "junit" % "junit" % "4.11" % Test
    )
  )

lazy val `task-impl` = (project in file("task-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lombok,
      "junit" % "junit" % "4.11" % Test
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`task-api`)

lazy val `board-api` = (project in file("board-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok,
      "junit" % "junit" % "4.11" % Test
    )
  )

lazy val `board-impl` = (project in file("board-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lombok,
      "junit" % "junit" % "4.11" % Test
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`board-api`)

val lombok = "org.projectlombok" % "lombok" % "1.16.10"

def common = Seq(
  javacOptions in compile += "-parameters"
)

