package acsgh.metrics.scala

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.TimeUnit

trait Metric {
  def values: Map[String, AnyVal]

  def reset(): Unit
}

trait Gauge extends Metric {
  def value: AnyVal

  override def values: Map[String, AnyVal] = Map("value" -> value)
}

case class Counter() extends Metric {
  private val count = new AtomicLong(0)

  def inc(delta: Long = 0): Unit = count.addAndGet(delta)

  def dec(delta: Long = 0): Unit = count.addAndGet(-delta)

  def reset(): Unit = count.set(0)

  override def values: Map[String, AnyVal] = Map("count" -> count.get())
}

sealed trait StoredMetric[T <: AnyVal] extends Metric {

  private var metricPoints = ListBuffer[T]()

  protected def update(value: T): Unit = {
    metricPoints.addOne(value)
  }

  def reset(): Unit = {
    metricPoints.clear()
  }

  override def values: Map[String, AnyVal] = Map("count" -> metricPoints.size)
}

case class Timer() extends StoredMetric[Long] {
  def update(amount: Long, unit: TimeUnit): Unit = {
    if (unit.ordinal() < TimeUnit.MILLISECONDS.ordinal()) {
      throw new IllegalArgumentException("The minimum time unit supported is milliseconds")
    }
    update(TimeUnit.MILLISECONDS.convert(amount, unit))
  }
}

case class Meter() extends StoredMetric[Double] {
  override def update(amount: Double): Unit = super.update(amount)
}

case class Histogram() extends StoredMetric[Double] {
  override def update(amount: Double): Unit = super.update(amount)
}
