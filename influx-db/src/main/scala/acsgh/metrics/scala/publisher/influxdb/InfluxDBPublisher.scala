package acsgh.metrics.scala.publisher.influxdb

import java.time.Instant
import java.util.concurrent.TimeUnit

import acsgh.metrics.scala.model.{MetricKey, PublishStrategy, Reset}
import acsgh.metrics.scala.{MetricsPublisher, MetricsRegistry}
import com.acsgh.common.scala.log.LogLevel
import com.acsgh.common.scala.time.StopWatch
import org.influxdb.dto.{Point, Pong}
import org.influxdb.{InfluxDB, InfluxDBFactory}

import scala.jdk.CollectionConverters._

case class InfluxDBPublisher
(
  private val metricsRegistry: MetricsRegistry,
  private val metricsConfig: InfluxDBConfig,
  private val commonTags: Map[String, String] = Map(),
  private val publishStrategy: PublishStrategy = PublishStrategy.Clean
) extends MetricsPublisher[InfluxDBConfig](metricsRegistry, metricsConfig, commonTags, publishStrategy) {

  private var database: InfluxDB = _

  def ping: Pong = database.ping

  override def publish(): Unit = {
    val stopWatch = StopWatch.createStarted()
    log.debug("Sending metrics...")
    try {
      metricsRegistry.snapshot.foreach { s =>
        try
          sendMetric(s.key, s.timeStamp, s.values)
        catch {
          case e: Exception => log.info(s"Unable to send point: ${s.key}", e)
        }
      }
      afterPublish()
    } catch {
      case e: Exception =>
        log.error("Unable to send metrics", e)
    } finally {
      stopWatch.printElapseTime("Sent metrics", log, LogLevel.DEBUG)
    }
  }

  override def onStart(): Unit = {
    database = {
      log.info("Influx DB: {}, DB: {}, Ret. Policy: {}", metricsConfig.url, metricsConfig.db, metricsConfig.retentionPolicy)

      InfluxDBFactory.connect(metricsConfig.url, metricsConfig.username, metricsConfig.password)
        .setDatabase(metricsConfig.db)
        .setRetentionPolicy(metricsConfig.retentionPolicy)
    }
  }

  override def onStop(): Unit = {
    database.close()
    database = null
  }

  protected def sendMetric(key: MetricKey, timestamp: Instant, values: Map[String, AnyVal]): Unit = {
    val tags: Map[String, String] = (commonTags ++ key.tags).filter(_._2 != null).filter(_._2.nonEmpty)
    val fields: Map[String, AnyRef] = values.view.mapValues(toField).toMap.filter(_._2.isDefined).view.mapValues(_.get).toMap

    if (fields.nonEmpty) {
      val point = Point.measurement(key.name)
        .fields(fields.asJava)
        .tag(tags.asJava)
        .time(timestamp.toEpochMilli, TimeUnit.MILLISECONDS)
        .build

      database.write(point)
    } else {
      log.warn("Point {} and timestamp {} has no fields", key, timestamp)
    }
  }

  private def afterPublish(): Unit = publishStrategy match {
    case PublishStrategy.Reset => metricsRegistry.reset()
    case PublishStrategy.Clean => metricsRegistry.clean((_, m) => m.isInstanceOf[Reset])
    case _ => // Do nothing
  }

  private def toField(value: AnyVal): Option[AnyRef] = {
    val result = value match {
      case v: Byte =>
        Some(v.toLong)
      case v: Short =>
        Some(v.toLong)
      case v: Int =>
        Some(v.toLong)
      case v: Long =>
        Some(v)
      case v: Float =>
        Some(v.toDouble).filterNot(v => v.isNaN || v.isInfinite)
      case v: Double =>
        Some(v).filterNot(v => v.isNaN || v.isInfinite)
      case v: Boolean =>
        Some(v)
      case rest =>
        Some(rest.toString)
    }

    result.asInstanceOf[Option[AnyRef]]
  }
}
