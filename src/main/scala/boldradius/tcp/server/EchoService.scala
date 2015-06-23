package boldradius.tcp.server

import akka.actor.{Actor, ActorLogging, Props}

import scala.language.implicitConversions

/**
 * @author Dmitri Carpov
 */
object EchoService {
  def props = Props(new EchoService)
}

class EchoService extends Actor with ActorLogging {

  import akka.io.Tcp._

  override def receive: Receive = {
    case Received(data) =>
      sender ! Write(data)

    case PeerClosed =>
      context stop self
  }
}
