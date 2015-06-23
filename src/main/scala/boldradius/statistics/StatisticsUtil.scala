package boldradius.statistics

/**
 * @author Dmitri Carpov
 */
object StatisticsUtil {
  def round(number: Double, scale: Int = 2): Double = BigDecimal(number).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble
}
