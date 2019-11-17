package acsgh.metrics.scala

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import acsgh.metrics.scala.model._
import com.acsgh.common.scala.log.LogSupport

abstract class MetricsPublisher[C <: MetricsConfig](private val registry: MetricsRegistry, metricsConfig: C) extends LogSupport {

  private val started = new AtomicBoolean()
  private var executorService: ScheduledExecutorService = _

  def start(): Unit = {
    if (started.compareAndSet(false, true)) {
      onStart()
      executorService = Executors.newScheduledThreadPool(1, (runnable: Runnable) => new Thread(runnable, "MetricPublisher"))
      executorService.scheduleAtFixedRate(() => publish(), metricsConfig.publishIntervalSecond, metricsConfig.publishIntervalSecond, TimeUnit.SECONDS)
    }
  }

  def stop(): Unit = {
    if (started.compareAndSet(true, false)) {
      try {
        publish()
      } catch {
        case e: Exception =>
          log.warn("Unable to publish stats", e)
      } finally {
        onStop()
        executorService.shutdown()
        executorService = _
      }
    }
  }

  def publish(): Unit

  def onStart(): Unit

  def onStop(): Unit
}
