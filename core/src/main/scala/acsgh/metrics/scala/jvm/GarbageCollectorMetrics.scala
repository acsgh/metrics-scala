package acsgh.metrics.scala.jvm

import java.lang.management.{GarbageCollectorMXBean, ManagementFactory}

import acsgh.metrics.scala.implicits._
import acsgh.metrics.scala.model.Metric

import scala.jdk.CollectionConverters._

case class GarbageCollectorMetrics
(
  garbageCollectors: List[GarbageCollectorMXBean] = List() ++ ManagementFactory.getGarbageCollectorMXBeans.asScala
) extends Metric {
  override def values: Map[String, AnyVal] = {
    garbageCollectors.foldLeft(Map[String, AnyVal]()) { (acc, garbageCollector) =>
      val name = garbageCollector.getName.withoutSpaces

      acc ++ Map(
        s"$name.count" -> garbageCollector.getCollectionCount,
        s"$name.time" -> garbageCollector.getCollectionTime,
      )
    }
  }
}
