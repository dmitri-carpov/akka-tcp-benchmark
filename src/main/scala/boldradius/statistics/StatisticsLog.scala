package boldradius.statistics

import StatisticsUtil._

/**
 * @author Dmitri Carpov
 */
case class StatisticsLog(
                          activeConnections: NumberOfConnections = 0,
                          failedConnections: NumberOfConnections = 0,
                          meanResponsesPerSecond: Mean = 0,
                          minResponseTime: Option[Milliseconds] = None,
                          maxResponseTime: Option[Milliseconds] = None,
                          meanResponseTime: Option[Mean] = None,
                          responses: Seq[Timestamp] = Seq.empty,
                          durations: Seq[Milliseconds] = Seq.empty
                          ) {

  def logConnection = copy(activeConnections = activeConnections + 1)

  def logDisconnection = copy(activeConnections = if (activeConnections < 1) 0 else activeConnections - 1)

  def logConnectionFailure = copy(failedConnections = failedConnections + 1)

  def logResponse(receivedTimestamp: Timestamp, duration: Milliseconds) = {
    copy(
      minResponseTime = minResponseTime.map(Math.min(_, duration)).orElse(Some(duration)),
      maxResponseTime = maxResponseTime.map(Math.max(_, duration)).orElse(Some(duration)),
      responses = responses.+:(receivedTimestamp),
      durations = durations.+:(duration)
    )
  }

  def withMeans: StatisticsLog = {
    if (!responses.isEmpty && !durations.isEmpty) {
      val duration = (responses.max - responses.min) / 1000.0
      copy(
        meanResponsesPerSecond = if (duration < 1) responses.size else responses.size / duration,
        meanResponseTime = Some(durations.sum.toDouble / durations.size)
      )
    } else this
  }


  override def toString: String = {
    val unknown = "unknown"
    val ms = " ms"
    val averageResponseTime = meanResponseTime.fold(unknown)(round(_) + ms)
    val minimumResponseTime = minResponseTime.fold(unknown)(_ + ms)
    val maximumResponseTime = maxResponseTime.fold(unknown)(_ + ms)

    s"\n\n---- Connections -----------------------" +
      s"\n> Active\t\t\t$activeConnections" +
      s"\n> Failed\t\t\t$failedConnections" +
      s"\n\n---- Responses ----------------------" +
      s"\n> Average responses per second\t${round(meanResponsesPerSecond)}" +
      s"\n> Average response time\t\t$averageResponseTime" +
      s"\n> Minimum response time\t\t$minimumResponseTime" +
      s"\n> Maximum response time\t\t$maximumResponseTime" +
      "\n----------------------------------------"
  }
}
