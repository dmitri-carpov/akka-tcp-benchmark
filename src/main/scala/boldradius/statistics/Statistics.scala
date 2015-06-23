package boldradius.statistics

import akka.actor._

import scala.language.postfixOps

/**
 * @author Dmitri Carpov
 */
object Statistics {
  def props = Props(new Statistics)

  case object Connected

  case object Disconnected

  case class ResponseReceived(received: Timestamp, duration: Milliseconds)

  case object ConnectionFailed

  case object Print

  case object Log

  case object Stop

}

class Statistics extends Actor with ActorLogging {

  import boldradius.statistics.Statistics._

  override def receive: Receive = collect(StatisticsLog())

  private def collect(statisticsLog: StatisticsLog): Receive = {

    case Connected =>
      context.become(collect(statisticsLog.logConnection))

    case Disconnected =>
      context.become(collect(statisticsLog.logDisconnection))

    case ConnectionFailed =>
      context.become(collect(statisticsLog.logConnectionFailure))

    case ResponseReceived(receivedTimestamp, duration) =>
      context.become(collect(statisticsLog.logResponse(receivedTimestamp, duration)))

    case Log =>
      sender ! statisticsLog.withMeans

    case Print =>
      println(statisticsLog.withMeans.toString)

    case Stop =>
      self ! PoisonPill
  }
}
