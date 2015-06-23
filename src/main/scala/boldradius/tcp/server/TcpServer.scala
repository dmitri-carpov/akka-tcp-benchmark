package boldradius.tcp.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.{IO, Tcp}

/**
 * @author Dmitri Carpov
 */
object TcpServer {
  def props(host: String, port: Int, handlerProps: Props) = Props(new TcpServer(host, port, handlerProps))
}

class TcpServer(host: String, port: Int, handlerProps: Props) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))

  override def receive: Receive = {
    case b@Bound(address) =>
      log.info(s"Bound on ${address.getHostName}:${address.getPort}")

    case CommandFailed =>
      log.info("Binding failed.")
      context stop self

    case c@Connected(remote, local) =>
      val handler = context.actorOf(handlerProps)
      sender() ! Register(handler)
  }
}
