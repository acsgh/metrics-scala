package acsgh.metrics.scala

import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import acsgh.metrics.scala.jvm._
import acsgh.metrics.scala.model._
import com.acsgh.common.scala.log.LogSupport

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object MetricsRegistry {

  lazy val default: MetricsRegistry = newInstance()

  def newInstance(): MetricsRegistry = {
    val registry: MetricsRegistry = new MetricsRegistry()
    DEFAULT_METRICS.foreach(m => registry.registerMetric(MetricKey(m._1), m._2))
    registry
  }
}

case class MetricsRegistry
(
  private val tickIntervalSecond: Long = 5
) extends LogSupport {

  private val metrics: mutable.Map[MetricKey, Metric] = new ConcurrentHashMap[MetricKey, Metric]().asScala
  private val started = new AtomicBoolean()
  private val executorService = Executors.newScheduledThreadPool(1, (runnable: Runnable) => new Thread(runnable, "MetricRegistry"))

  def start(): Unit = {
    if (started.compareAndSet(false, true)) {
      executorService.scheduleAtFixedRate(() => tick(), tickIntervalSecond, tickIntervalSecond, TimeUnit.SECONDS)
    }
  }

  def stop(): Unit = {
    if (started.compareAndSet(true, false)) {
      log.debug("Stopping influx connection")
    }
  }

  def registerMetric(key: MetricKey, metric: Metric): Unit = registerMetricIfNotPresent(key, metric)

  def counterUpdate(key: MetricKey, value: Long = 1): Unit = registerMetricIfNotPresent(key, Counter()).inc(value)

  def samplerUpdate(key: MetricKey, value: Long = 1): Unit = registerMetricIfNotPresent(key, Sampler()).update(value)

  def rateUpdate(key: MetricKey, value: Long = 1): Unit = registerMetricIfNotPresent(key, Rate()).hit(value)

  def percentileUpdate(key: MetricKey, value: Long = 1): Unit = registerMetricIfNotPresent(key, Percentile()).update(value)

  def meterUpdate(key: MetricKey, value: Long = 1): Unit = registerMetricIfNotPresent(key, Meter()).hit(value)

  def timerUpdate(key: MetricKey, value: Long, unit: TimeUnit): Unit = registerMetricIfNotPresent(key, Timer()).update(value, unit)

  def histogramUpdate(key: MetricKey, value: Long): Unit = registerMetricIfNotPresent(key, Histogram()).update(value)

  //      clean((_, value) => !value.isInstanceOf[CleanAfterPublishMetric])
  def clean(validator: (MetricKey, Metric) => Boolean = (_, _) => true): Unit = metrics.filterInPlace(validator)

  def reset(validator: (MetricKey, Metric) => Boolean = (_, _) => true): Unit = metrics.filter(v => validator(v._1, v._2)).values
    .filter(_.isInstanceOf[Reset])
    .map(_.asInstanceOf[Reset])
    .foreach(_.reset())

  def snapshot: List[MetricSnapshot] = metrics.map(e => MetricSnapshot(e._1, Instant.now(), e._2.values)).toList.sortBy(_.key.name)

  private def tick(): Unit = metrics.values.filter(_.isInstanceOf[Ticks]).map(_.asInstanceOf[Ticks]).foreach(_.tick())

  private def registerMetricIfNotPresent[T <: Metric](key: MetricKey, metric: T): T = {
    metrics.get(key).fold({
      metrics += (key -> metric)
      metric
    }) {
      case t: T =>
        t
      case storedMetric =>
        throw new IllegalArgumentException(s"Metric $key is already register as ${storedMetric.getClass.getSimpleName}")
    }
  }
}
