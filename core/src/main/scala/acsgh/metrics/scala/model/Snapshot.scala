package acsgh.metrics.scala.model

import java.time.Instant

import acsgh.metrics.scala.implicits._

object Snapshot {
  def build(from: Instant, values: List[MetricEntry]): Snapshot = {
    val to = Instant.now()
    val sortedValues = values.map(_.value).sorted

    val count: Long = sortedValues.size
    val min: Long = sortedValues.min
    val max: Long = sortedValues.max
    val sum: Long = sortedValues.sum
    val mean: Double = if (count > 0) (sum.toDouble / count).scale() else 0
    val stdDev: Double = {
      if (count > 0) {
        val buffer: Double = sortedValues.map(_ - mean).map(v => v * v).sum / count
        buffer.sqrt.scale()
      } else {
        0
      }
    }

    def percentile(percentile: Double): Long = {
      assert(percentile > 0)
      assert(percentile < 100)

      val index: Int = (percentile * count).toInt
      sortedValues(index)
    }

    def rate(seconds: Long): Double = {
      assert(seconds > 0)

      val totalSeconds = ((to.toEpochMilli - from.toEpochMilli) / 1000).toInt

      if (seconds < totalSeconds) {
        values.count(_.timestamp.compareTo(from.plusSeconds(seconds)) <= 0)
      } else {
        (seconds * count) / totalSeconds
      }
    }

    Snapshot(
      from,
      to,
      count,
      min,
      max,
      sum,
      mean,
      stdDev,
      List(60, 300, 900).map(s => s.toLong -> rate(s)).toMap,
      List(5, 25, 50, 75, 90, 99, 99.5).map(p => p -> percentile(p)).toMap
    )
  }
}

case class Snapshot
(
  from: Instant,
  to: Instant,
  count: Long,
  min: Long,
  max: Long,
  sum: Long,
  mean: Double,
  stdDev: Double,
  private val rates: Map[Long, Double],
  private val percentiles: Map[Double, Long]
) {
  def rate(seconds: Long): Option[Double] = rates.get(seconds)

  def percentile(percentile: Double): Option[Long] = percentiles.get(percentile)

  def eventFields: Map[String, AnyVal] = Map("events" -> count)

  def histogramFields: Map[String, AnyVal] = Map(
    "total" -> sum,
    "min" -> min,
    "max" -> max,
    "mean" -> mean,
    "std-dev" -> stdDev,
    "p05" -> percentile(5).getOrElse(0),
    "p25" -> percentile(25).getOrElse(0),
    "p50" -> percentile(50).getOrElse(0),
    "p75" -> percentile(75).getOrElse(0),
    "p90" -> percentile(90).getOrElse(0),
    "p99" -> percentile(99).getOrElse(0),
    "p995" -> percentile(99.5).getOrElse(0)
  )

  def meterFields: Map[String, AnyVal] = {
    val ratesSum = rates.values.sum
    val ratesSize = rates.values.size
    val rateMean = if (ratesSize > 0) ratesSum / ratesSize else 0

    Map(
      "mean-rate" -> rateMean,
      "1m-rate" -> rate(60).getOrElse(0),
      "5m-rate" -> rate(300).getOrElse(0),
      "5m-rate" -> rate(900).getOrElse(0)
    )
  }
}