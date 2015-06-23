name := "akka-tcp-benchmark"

version := "0.1"

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies := Seq(
  Library.akka,
  Library.scalaz,
  Library.akkatest,
  Library.scalatest,
  Library.scalacheck
)

