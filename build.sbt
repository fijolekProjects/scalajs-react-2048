import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "scala-2048"

version := "0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  Seq(
    "org.scalaz" %% "scalaz-core" % "7.1.3",
    "com.chuusai" %% "shapeless" % "2.2.4",
    "com.nicta" %% "rng" % "1.3.0",
    "org.scalacheck" % "scalacheck_2.11" % "1.12.4" % "test",
    "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
  )
}

resolvers += "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "releases"  at "https://oss.sonatype.org/content/groups/scala-tools"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

scalaJSStage in Global := FastOptStage

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.8.0"

libraryDependencies += "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.1.3"

libraryDependencies += "com.github.japgolly.fork.nicta" %%% "rng" % "1.3.0"

libraryDependencies += "com.github.japgolly.scalajs-react" % "core_sjs0.6_2.11" % "0.10.1"

// React JS itself (Note the filenames, adjust as needed, eg. to remove addons.)
jsDependencies += "org.webjars.npm" % "react"     % "0.14.2" / "react-with-addons.js" commonJSName "React"    minified "react-with-addons.min.js"

jsDependencies += "org.webjars.npm" % "react-dom" % "0.14.2" / "react-dom.js"         commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js"

bootSnippet := "webapp.Game2048Webapp().main();"

refreshBrowsers <<= refreshBrowsers.triggeredBy(fastOptJS in Compile)
