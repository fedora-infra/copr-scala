name := "coprscala"

organization := "org.fedoraproject"

version := "1.0.0"

publishTo := Some(Resolver.file("file", new File( "releases" )) )

crossPaths := false

scalaVersion := "2.11.2"

resolvers += Resolver.sonatypeRepo("releases")

resolvers += Resolver.sonatypeRepo("snapshots")

//addCompilerPlugin("org.brianmckenna" %% "wartremover" % "0.8")

// TODO: Figure out a way to remove cast to HTTPUrlConnection that causes this to error...
//scalacOptions in (Compile, compile) += "-P:wartremover:traverser:org.brianmckenna.wartremover.warts.Unsafe"

publishTo := Some(Resolver.file("file", new File("releases")))

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

libraryDependencies += "io.argonaut" %% "argonaut" % "6.1-M4"

libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.1.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "test"

// Lens!
libraryDependencies ++= Seq(
  "com.github.julien-truffaut"  %%  "monocle-core"    % "0.5.1",
  "com.github.julien-truffaut"  %%  "monocle-generic" % "0.5.1",
  "com.github.julien-truffaut"  %%  "monocle-macro"   % "0.5.1",
  "com.github.julien-truffaut"  %%  "monocle-law"     % "0.5.1" % "test"
)
