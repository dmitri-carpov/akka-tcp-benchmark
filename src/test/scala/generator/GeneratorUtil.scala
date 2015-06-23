package generator

import org.scalacheck.Gen

/**
 * @author Dmitri Carpov
 */
object GeneratorUtil {
  def one[A](gen: Gen[A]): A = gen.sample.getOrElse(throw new RuntimeException("Cannot generate value"))
}
