package acsgh.metrics.scala.jvm

import java.lang.management.{ClassLoadingMXBean, ManagementFactory}

import acsgh.metrics.scala.model.Metric

case class ClassLoadingMetrics
(
  mxBean: ClassLoadingMXBean = ManagementFactory.getClassLoadingMXBean
) extends Metric {
  override def values: Map[String, AnyVal] = Map(
    "loaded" -> mxBean.getTotalLoadedClassCount,
    "unloaded" -> mxBean.getUnloadedClassCount,
  )
}
