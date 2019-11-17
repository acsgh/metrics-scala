package acsgh.metrics.scala.jvm

import acsgh.metrics.scala.model.Metric

case class CPUMetrics() extends Metric with JMXServerSupport {
  override def values: Map[String, AnyVal] = Map(
    "ProcessCpuLoad" -> getOSAttributeDouble("ProcessCpuLoad"),
    "ProcessCpuTime" -> getOSAttributeLong("ProcessCpuTime")
  )
}
