name := "auth"
organization := "net.jsfwa"

version := "0.4.1b"

scalaVersion := "2.12.4"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  guice,
  ehcache,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.scalactic" %% "scalactic" % "3.0.4",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

scalaSource in Compile := baseDirectory.value / "src"

publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.ivy2/local")))

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

mappings in (Compile, packageBin) ~= { _.filterNot { case (_, n) =>
  n.startsWith("sample")
}}

sources in (Compile,doc) := Seq.empty