package acsgh.metrics.scala.model

import java.time.Instant
import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import acsgh.metrics.scala.implicits._
import com.acsgh.common.scala.lock.ReentrantReadWriteLock

import scala.collection.immutable.ListMap
import scala.concurrent.duration.{Duration, TimeUnit, _}

case class MetricKey(name: String, tags: Map[String, String] = Map())

case class MetricSnapshot(key: MetricKey, timeStamp: Instant, values: Map[String, AnyVal])

trait Metric {
  def values: Map[String, AnyVal]

  protected def merge(values: Map[String, AnyVal]*): Map[String, AnyVal] = {
    values.flatMap(_.toList).foldLeft(ListMap[String, AnyVal]())((acc, e) => acc + e)
  }
}

trait Reset {
  def reset(): Unit
}

trait Ticks {
  def tick(): Unit
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

case class Sampler() extends Metric with Reset {
  private val lock = new ReentrantReadWriteLock()

  private var _total: BigDecimal = 0
  private var _min: BigDecimal = 0
  private var _max: BigDecimal = 0
  private var _mean: BigDecimal = 0
  private var _events: Long = 0

  def total: BigDecimal = lock.read(_total)

  def min: BigDecimal = lock.read(_min)

  def max: BigDecimal = lock.read(_max)

  def mean: BigDecimal = lock.read(_mean)

  def events: BigDecimal = lock.read(_events)

  def update(value: BigDecimal): Unit = lock.write {
    _total = _total + value
    _min = if (events > 0) List(_min, value).min else value
    _max = List(_max, value).max
    _mean = ((_events * _mean) + value) / (_events + 1)
    _events = _events + 1
  }

  override def reset(): Unit = lock.write {
    _total = 0
    _min = 0
    _max = 0
    _mean = 0
    _events = 0
  }

  override def values: Map[String, AnyVal] = lock.read(
    ListMap(
      "events" -> _events,
      "total" -> _total.toDouble.scale(),
      "min" -> _min.toDouble.scale(),
      "max" -> _max.toDouble.scale(),
      "mean" -> _mean.toDouble.scale()
    )
  )
}

case class Percentile() extends Metric with Reset {
  private val lock = new ReentrantReadWriteLock()

  private val _values: util.ArrayList[BigDecimal] = new util.ArrayList()

  def percentile(percentile: Double): BigDecimal = lock.read(percentileInt(percentile))

  def update(value: BigDecimal): Unit = lock.write {
    _values.add(value)
    util.Collections.sort(_values)
  }

  override def reset(): Unit = lock.write {
    _values.clear()
  }

  override def values: Map[String, AnyVal] = lock.read(
    ListMap(
      "p-05%" -> percentileInt(5).toDouble.scale(),
      "p-25%" -> percentileInt(25).toDouble.scale(),
      "p-50%" -> percentileInt(50).toDouble.scale(),
      "p-75%" -> percentileInt(75).toDouble.scale(),
      "p-90%" -> percentileInt(90).toDouble.scale(),
      "p-99%" -> percentileInt(99).toDouble.scale(),
      "p-99.5%" -> percentileInt(99.5).toDouble.scale()
    )
  )

  private def percentileInt(percentile: Double): BigDecimal = {
    assert(percentile > 0)
    assert(percentile < 100)

    val count = _values.size
    if (count > 0) {
      val index: Int = Math.max(0, ((percentile / 100) * count).ceil.toInt - 1)
      _values.get(index)
    } else {
      0
    }
  }
}

case class Rate(period: Duration = 1 minute, timeProvider: () => Instant = () => Instant.now()) extends Metric with Reset with Ticks {
  private val lock = new ReentrantReadWriteLock()

  private var times: List[Instant] = List()

  def rate: BigDecimal = lock.read(times.size)

  def hit(amount: Long = 1): Unit = lock.write {
    val now = timeProvider()
    times = times ++ (0.toLong until amount).map(_ => now).toList
  }

  override def tick(): Unit = lock.write {
    val cutTime = timeProvider().minusSeconds(period.toSeconds)
    times = times.filter(v => v.compareTo(cutTime) >= 0)
  }

  override def reset(): Unit = lock.write {
    times = List()
  }

  override def values: Map[String, AnyVal] = lock.read {
    ListMap(
      "rate" -> times.size,
    )
  }
}

case class Timer() extends Metric with Reset {
  private val summary: Sampler = Sampler()
  private val percentile: Percentile = Percentile()
  private val rate: Rate = Rate()

  def update(amount: Long, unit: TimeUnit): Unit = {
    if (unit.ordinal() < TimeUnit.MILLISECONDS.ordinal()) {
      throw new IllegalArgumentException("The minimum time unit supported is milliseconds")
    }

    val value = TimeUnit.MILLISECONDS.convert(amount, unit)
    summary.update(value)
    percentile.update(value)
    rate.hit()
  }

  override def values: Map[String, AnyVal] = merge(summary.values, percentile.values, rate.values)

  override def reset(): Unit = {
    summary.reset()
    percentile.reset()
    rate.reset()
  }
}

case class Meter() extends Metric with Reset with Ticks {
  private val sampling = Sampler()
  private val rate = Rate()

  def hit(amount: Long = 0): Unit = {
    sampling.update(amount)
    rate.hit(amount)
  }

  override def reset(): Unit = {
    sampling.reset()
    rate.reset()
  }

  override def tick(): Unit = rate.tick()

  override def values: Map[String, AnyVal] = merge(sampling.values, rate.values)

}

case class Histogram() extends Metric with Reset {
  private val summary: Sampler = Sampler()
  private val percentile: Percentile = Percentile()

  def update(value: BigDecimal): Unit = {
    summary.update(value)
    percentile.update(value)
  }

  override def values: Map[String, AnyVal] = merge(summary.values, percentile.values)

  override def reset(): Unit = {
    summary.reset()
    percentile.reset()
  }
}