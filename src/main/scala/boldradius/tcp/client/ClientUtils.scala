package boldradius.tcp.client

import akka.actor.{ActorSystem, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz._


/**
 * @author Dmitri Carpov
 */
object ClientUtils {
  /**
   * Start actors with interval between 2ms and 100ms.
   *
   * @param numberOfActors Number of actors to start.
   * @param props Actor's Props object.
   * @param system Actor system.
   */
  def startActors(numberOfActors: Int, props: Props)(implicit system: ActorSystem): Unit = {
    val ratio = 10000 / numberOfActors
    val interval: Int = if (ratio > 100) 100 else if(ratio < 2) 2 else ratio
    (1 to numberOfActors).foreach(n => system.scheduler.scheduleOnce((n * interval) milliseconds)(system.actorOf(props)))
  }

  /**
   * Parse arguments.
   * Expects:
   * 0 - host (string)
   * 1 - port (int)
   * 2 - number of connections (int)
   *
   * @param args Arguments to parse
   * @return Validation object with host, port and number of connections.
   */
  def parseArguments(args: Array[String]): Validation[String, (Host, Port, NumberOfClients, FiniteDuration)] = {
    def parseArgument[A](
                          argName: String,
                          arg: => String,
                          validator: (String) => Validation[String, A]): Validation[String, A] = try {
      validator(arg)
    } catch {
      case _: IndexOutOfBoundsException => Failure(s"$argName is not specified\n")
    }

    (parseArgument[String]("host", args(0), notEmpty) ⊛
      parseArgument[Int]("port", args(1), parseInt) ⊛
      parseArgument[Int]("number of connections", args(2), parseInt) ⊛
      parseArgument[FiniteDuration]("sleep time", args(3), parseDuration)
      ) {
      (_, _, _, _)
    }
  }

  /**
   * Parse string to integer.
   *
   * @param string String to parse.
   * @return Validation object.
   */
  def parseInt(string: String): Validation[String, Int] = try {
    Success(string.toInt)
  } catch {
    case e: NumberFormatException =>
      Failure(s"$string is not an integer number\n")
  }

  /**
   * Parse string to int but return it as a duration in milliseconds.
   *
   * @param string String to parse.
   * @return Validation object.
   */
  def parseDuration(string: String): Validation[String, FiniteDuration] = try {
    Success(string.toInt milliseconds)
  } catch {
    case e: NumberFormatException =>
      Failure(s"$string is not an integer number\n")
  }

  /**
   * Validate if string is not empty.
   *
   * @param string String to validate.
   * @return Validation object.
   */
  def notEmpty(string: String): Validation[String, String] = if (string.isEmpty) Failure("string is empty") else Success(string)

  /**
   * Call function f only if the validation function returns true otherwise return the default value.
   *
   * @param data Data object.
   * @param validation Data validation function.
   * @param f Conversion function from data type to type B
   * @param default Default value.
   * @tparam A Data type.
   * @tparam B Result type.
   * @return result of function f or default value.
   */
  def applyIf[A, B](data: A, validation: A => Boolean, f: A => B, default: B): B = if (validation(data)) f(data) else default

}
