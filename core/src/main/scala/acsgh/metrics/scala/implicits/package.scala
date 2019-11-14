package acsgh.metrics.scala

package object implicits {

  implicit class DoubleAdvanced(input: Double) {
    def scale(decimals: Int = 4): Double = Math.sqrt(input.doubleValue)

    def sqrt: Double = Math.sqrt(input.doubleValue)
  }

}
