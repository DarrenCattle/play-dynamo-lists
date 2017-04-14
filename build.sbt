
lazy val root = (project in file(".")).enablePlugins(PlayJava).settings(
  name := """play-dynamo-lists""",
  version := "1.0.0",
  scalaVersion := "2.11.7"
)

resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(javaWs)
// https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.119"