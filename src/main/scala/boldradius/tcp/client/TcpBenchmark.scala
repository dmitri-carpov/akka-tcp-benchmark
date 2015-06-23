package boldradius.tcp.client

import java.net.InetSocketAddress
import java.util.UUID

import akka.actor._
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import boldradius.statistics._
import boldradius.tcp.client.TcpBenchmark.{Stop, SendData}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.language.postfixOps

/**
 * @author Dmitri Carpov
 */
object TcpBenchmark {
  def props(host: String, port: Int, statistics: ActorRef, sleepTime: FiniteDuration) =
    Props(new TcpBenchmark(host, port, statistics, sleepTime))

  case object SendData
  case object Stop
}

class TcpBenchmark(host: String, port: Int, statistics: ActorRef, sleepTime: FiniteDuration) extends Actor with ActorLogging {

  import context.system

  IO(Tcp) ! Connect(new InetSocketAddress(host, port))

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      statistics ! Statistics.ConnectionFailed
      context stop self

    case c@Connected(remote, local) =>
      val connection = sender()
      connection ! Register(self)
      statistics ! Statistics.Connected

      context.become(ready(connection, now))
      context.system.scheduler.scheduleOnce(1 second, self, SendData)
      context.system.scheduler.scheduleOnce(1 minute, self, Close)
  }

  def ready(connection: ActorRef, timestamp: Timestamp): Receive = {
    case SendData =>
      connection ! Write(ByteString(UUID.randomUUID().toString))
      context.become(ready(connection, now))

    case CommandFailed =>
      context.system.scheduler.scheduleOnce(1 second, self, SendData)

    case Received(data) =>
      statistics ! Statistics.ResponseReceived(now, now - timestamp)
      if (sleepTime.toMillis > 0) {
        context.system.scheduler.scheduleOnce(sleepTime, self, SendData)
      } else {
        self ! SendData
      }

    case Stop =>
      connection ! Close

    case _: ConnectionClosed =>
      context stop self
  }


  private def now: Timestamp = System.currentTimeMillis()
}
