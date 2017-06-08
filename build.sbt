lazy val root = (project in file(".")).enablePlugins(PlayJava).settings(
  name := """play-dynamo-lists""",
  version := "1.0.0",
  scalaVersion := "2.11.7"
)

libraryDependencies ++= Seq(javaWs)
// https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.119"