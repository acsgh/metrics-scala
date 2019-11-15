package acsgh.metrics.scala.model

import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.TimeUnit

trait Metric {
  def values: Map[String, AnyVal]
}

trait Reset {
  def reset(): Unit
}

trait Gauge extends Metric {
  def value: AnyVal

  override def values: Map[String, AnyVal] = Map("value" -> value)
}

case class Counter() extends Metric with Reset {
  private val _count = new AtomicLong(0)

  def count: Long = _count.get()

  def inc(delta: Long = 1): Unit = _count.addAndGet(delta)

  def dec(delta: Long = 1): Unit = _count.addAndGet(-delta)

  override def reset(): Unit = _count.set(0)

  override def values: Map[String, AnyVal] = Map("count" -> _count.get())
}

case class MetricEntry
(
  value: Long,
  timestamp: Instant = Instant.now()
)

sealed trait StoredMetric extends Metric with Reset {

  private val metricPoints: ListBuffer[MetricEntry] = ListBuffer[MetricEntry]()
  private var from: Instant = Instant.now()

  protected def metric(value: Long): Unit = metricPoints.addOne(MetricEntry(value))

  def snapshot: Snapshot = Snapshot.build(from, metricPoints.toList)

  override def reset(): Unit = {
    metricPoints.clear()
    from = Instant.now()
  }
}

case class Timer() extends StoredMetric {
  def update(amount: Long, unit: TimeUnit): Unit = {
    if (unit.ordinal() < TimeUnit.MILLISECONDS.ordinal()) {
      throw new IllegalArgumentException("The minimum time unit supported is milliseconds")
    }
    metric(TimeUnit.MILLISECONDS.convert(amount, unit))
  }

  override def values: Map[String, AnyVal] = {
    val snap = snapshot
    snap.eventFields ++ snap.meterFields ++ snap.histogramFields
  }
}

case class Meter() extends StoredMetric {
  def update(amount: Long): Unit = metric(amount)

  override def values: Map[String, AnyVal] = {
    val snap = snapshot
    snap.eventFields ++ snap.meterFields
  }
}

case class Histogram() extends StoredMetric {
  def update(amount: Long): Unit = metric(amount)

  override def values: Map[String, AnyVal] = {
    val snap = snapshot
    snap.eventFields ++ snap.histogramFields
  }
}
