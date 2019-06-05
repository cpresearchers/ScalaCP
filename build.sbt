name := "ScalaCP"

version := "0.1"

scalaVersion := "2.12.8"

javacOptions ++= Seq("-encoding", "UTF-8")
javaOptions += "-Xms500m"
javaOptions += "-Xmx9000m"

libraryDependencies += "com.alibaba" % "fastjson" % "1.2.57"
libraryDependencies += "com.github.tototoshi" % "scala-csv_2.12" % "1.3.5"
libraryDependencies += "org.msgpack" % "msgpack-core" % "0.8.16"
libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.12.0-RC1" % "1.0.6"
libraryDependencies += "org.xcsp" % "xcsp3-tools" % "1.1.0"

mainClass in (Compile, run) := Some("cpscala.TSolver.Experiment.CPF_Tester")
