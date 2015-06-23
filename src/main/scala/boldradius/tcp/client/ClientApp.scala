package boldradius.tcp.client

import akka.actor.ActorSystem
import boldradius.statistics.Statistics
import ClientUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.{Failure, Success}

/**
 * @author Dmitri Carpov
 */
object ClientApp {

  def main(args: Array[String]): Unit = {
    parseArguments(args) match {
      case Success((host, port, numberOfConnections, sleepTime)) =>
        implicit val system = ActorSystem("AkkaTcpBenchmarkClient")

        val statistics = system.actorOf(Statistics.props)
        startActors(numberOfConnections, TcpBenchmark.props(host, port, statistics, sleepTime))

        println(
          s"Starting ${numberOfConnections} connections.\n" +
          s"Please wait 70 seconds until the benchmark is done.\n" +
          s"You can press ctrl-c after the report is printed.\n")

        system.scheduler.scheduleOnce(70 seconds, statistics, Statistics.Print)
      case Failure(message) =>
        println(s"\n$message\n\n" +
          "Usage: sbt \"run <host> <port> <number of connections> <sleep time between requests>\"\n" +
          "Example: sbt \"run localhost 2020 4000 0\"\n")
        System.exit(1)
    }
  }
}
