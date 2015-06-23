package tcpdemo.server

import akka.actor.ActorSystem
import boldradius.tcp.server.{EchoService, TcpServer}

/**
 * @author Dmitri Carpov
 */
object ServerApp extends App {
  implicit val actorSystem = ActorSystem("EchoServer")

  actorSystem.actorOf(TcpServer.props("0.0.0.0", 2020, EchoService.props))
}
