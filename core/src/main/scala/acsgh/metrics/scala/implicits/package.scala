package acsgh.metrics.scala

import java.util.regex.Pattern

package object implicits {

  implicit class DoubleAdvanced(input: Double) {
    def scale(decimals: Int = 4): Double = {
      val zeros = BigDecimal(10).pow(decimals).toInt
      val roundedNumber = (input * zeros).toInt
      roundedNumber.toDouble / zeros
    }

    def sqrt: Double = Math.sqrt(input.doubleValue)
  }

  implicit class StringAdvanced(input: String) {
    private val WHITESPACE = Pattern.compile("[\\s]+")

    def withoutSpaces: String = WHITESPACE.matcher(input).replaceAll("-")
  }

}
