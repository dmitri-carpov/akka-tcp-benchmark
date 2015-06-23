import sbt._

object Version {
  val akka       = "2.3.11"
  val scalaz     = "7.1.3"
  val scalatest  = "2.2.5"
  val scalacheck = "1.12.2"
}

object Library {
  val akka       = "com.typesafe.akka" %% "akka-actor"   % Version.akka
  val scalaz     = "org.scalaz"        %% "scalaz-core"  % Version.scalaz
  val akkatest   = "com.typesafe.akka" %% "akka-testkit" % Version.akka       % "test"
  val scalatest  = "org.scalatest"     %% "scalatest"    % Version.scalatest  % "test"
  val scalacheck = "org.scalacheck"    %% "scalacheck"   % Version.scalacheck % "test"
}
