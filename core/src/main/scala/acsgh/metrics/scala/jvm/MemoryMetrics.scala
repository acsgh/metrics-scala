package acsgh.metrics.scala.jvm

import java.lang.management.{ManagementFactory, MemoryMXBean, MemoryPoolMXBean}

import acsgh.metrics.scala.implicits._
import acsgh.metrics.scala.model.Metric

import scala.jdk.CollectionConverters._

case class MemoryMetrics
(
  mxBean: MemoryMXBean = ManagementFactory.getMemoryMXBean,
  memoryPools: List[MemoryPoolMXBean] = List() ++ ManagementFactory.getMemoryPoolMXBeans.asScala
) extends Metric {
  override def values: Map[String, AnyVal] = {
    val heapUsage = mxBean.getHeapMemoryUsage
    val nonHeapUsage = mxBean.getNonHeapMemoryUsage

    val totals = Map(
      "total.init" -> (heapUsage.getInit + nonHeapUsage.getInit),
      "total.used" -> (heapUsage.getUsed + nonHeapUsage.getUsed),
      "total.max" -> (heapUsage.getMax + nonHeapUsage.getMax),
      "total.committed" -> (heapUsage.getCommitted + nonHeapUsage.getCommitted),
      "heap.init" -> heapUsage.getInit,
      "heap.used" -> heapUsage.getUsed,
      "heap.max" -> heapUsage.getMax,
      "heap.committed" -> heapUsage.getCommitted,
      "heap.usage" -> (heapUsage.getUsed / heapUsage.getMax).scale(),
      "non-heap.init" -> nonHeapUsage.getInit,
      "non-heap.used" -> nonHeapUsage.getUsed,
      "non-heap.max" -> nonHeapUsage.getMax,
      "non-heap.committed" -> nonHeapUsage.getCommitted,
      "non-heap.usage" -> (nonHeapUsage.getUsed / nonHeapUsage.getMax).scale(),
    )

    val pools = memoryPools.foldLeft(Map[String, AnyVal]()) { (acc, pool) =>
      val poolName = pool.getName.withoutSpaces.replace("-'", ".").replace("'", "")
      val usage = pool.getUsage

      acc ++ Map(
        s"pool.$poolName.init" -> usage.getInit,
        s"pool.$poolName.used" -> usage.getUsed,
        s"pool.$poolName.max" -> usage.getMax,
        s"pool.$poolName.committed" -> usage.getCommitted,
        s"pool.$poolName.usage" -> (usage.getUsed / usage.getMax).scale(),
        s"pool.$poolName.used-after-gc" -> Option(pool.getCollectionUsage).map(_.getUsed).getOrElse(0)
      )
    }

    totals ++ pools
  }
}
