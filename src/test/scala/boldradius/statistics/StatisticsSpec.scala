package boldradius.statistics

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpecLike}

/**
 * @author Dmitri Carpov
 */
class StatisticsSpec extends TestKit(ActorSystem("testActorSystem"))
with ImplicitSender with WordSpecLike
with Matchers with GeneratorDrivenPropertyChecks {

  import boldradius.statistics.StatisticsUtil._

  "Statistics" when {
    "new connection is registered" must {
      "increase number of active connections" in {
        forAll(Gen.choose(1, 100)) { numberOfConnections =>
          val statistics = TestActorRef[Statistics]
          (1 to numberOfConnections).foreach(_ => statistics ! Statistics.Connected)

          statistics ! Statistics.Log
          val log = expectMsgType[StatisticsLog]
          assert(log.activeConnections == numberOfConnections)
        }
      }

      "a connection close event is registered" must {
        "decrease number of active connections" in {
          val generator = for {
            numberOfConnections <- Gen.choose(1, 20)
            numberOfDisconnections <- Gen.choose(1, numberOfConnections)
          } yield (numberOfConnections, numberOfDisconnections)

          forAll(generator) {
            case (numberOfConnections, numberOfDisconnections) =>
              val statistics = TestActorRef[Statistics]

              (1 to numberOfConnections).foreach(_ => statistics ! Statistics.Connected)
              (1 to numberOfDisconnections).foreach(_ => statistics ! Statistics.Disconnected)

              statistics ! Statistics.Log
              val log = expectMsgType[StatisticsLog]
              assert(log.activeConnections == numberOfConnections - numberOfDisconnections)
          }
        }

        "never have negative number of active connections" in {
          forAll(Gen.choose(1, 100)) { numberOfDisconnections =>
            val statistics = TestActorRef[Statistics]

            (1 to numberOfDisconnections).foreach(_ => statistics ! Statistics.Disconnected)

            statistics ! Statistics.Log
            val log = expectMsgType[StatisticsLog]
            assert(log.activeConnections == 0)
          }
        }
      }

      "a connection failed event is registered" must {
        "increase number of failed connections" in {
          forAll(Gen.choose(1, 100)) { numberOfFailures =>
            val statistics = TestActorRef[Statistics]

            (1 to numberOfFailures).foreach(_ => statistics ! Statistics.ConnectionFailed)

            statistics ! Statistics.Log
            val log = expectMsgType[StatisticsLog]
            assert(log.failedConnections == numberOfFailures)
          }
        }
      }
    }

    "response is registered" must {
      "calculate minimum response time" in {
        val generator = for {
          numberOrResponses <- Gen.choose(1, 100)
          durations <- Gen.listOfN(numberOrResponses, Gen.choose(1, 1000))
        } yield durations

        forAll(generator) { durations =>
          val statistics = TestActorRef[Statistics]

          durations.foreach { duration =>
            statistics ! Statistics.ResponseReceived(System.currentTimeMillis(), duration)
          }

          statistics ! Statistics.Log
          val log = expectMsgType[StatisticsLog]
          assert(log.minResponseTime.getOrElse(false) == durations.min)
        }
      }

      "calculate maximum response time" in {
        val generator = for {
          numberOrResponses <- Gen.choose(1, 100)
          durations <- Gen.listOfN(numberOrResponses, Gen.choose(1, 1000))
        } yield durations

        forAll(generator) { durations =>
          val statistics = TestActorRef[Statistics]

          durations.foreach { duration =>
            statistics ! Statistics.ResponseReceived(System.currentTimeMillis(), duration)
          }

          statistics ! Statistics.Log
          val log = expectMsgType[StatisticsLog]
          assert(log.maxResponseTime.getOrElse(false) == durations.max)
        }
      }

      "calculate average response time" in {
        val generator = for {
          numberOrResponses <- Gen.choose(1, 100)
          durations <- Gen.listOfN(numberOrResponses, Gen.choose(1, 1000))
        } yield durations

        forAll(generator) { durations =>
          val statistics = TestActorRef[Statistics]

          durations.foreach { duration =>
            statistics ! Statistics.ResponseReceived(System.currentTimeMillis(), duration)
          }

          statistics ! Statistics.Log
          val log = expectMsgType[StatisticsLog]
          log.meanResponseTime.fold(assert(false)) { result =>
            assert(round(result, 1) == round(durations.sum / durations.size.toDouble, 1))
          }
        }
      }

      "calculate average responses per second" in {
        val generator = for {
          numberOrResponses <- Gen.choose(1, 100)
          numberOfSeconds <- Gen.choose(1, 20)
          responseTimes <- Gen.listOfN(numberOrResponses, Gen.choose(1, numberOfSeconds * 1000))
        } yield (responseTimes, numberOfSeconds)

        forAll(generator) {
          case (responseTimes, numberOfSeconds) =>
            val statistics = TestActorRef[Statistics]

            val startPoint = System.currentTimeMillis()
            responseTimes.foreach { responseTime =>
              statistics ! Statistics.ResponseReceived(startPoint - responseTime, 100)
            }

            statistics ! Statistics.Log
            val log = expectMsgType[StatisticsLog]

            assert(log.meanResponsesPerSecond <= responseTimes.size &&
              log.meanResponsesPerSecond >= responseTimes.size / numberOfSeconds)
        }
      }
    }
  }
}
