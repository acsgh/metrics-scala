package acsgh.metrics.scala.jvm

import java.lang.management.{ManagementFactory, ThreadInfo, ThreadMXBean}

import acsgh.metrics.scala.model.Metric

import scala.collection.immutable.ListMap

case class ThreadMetrics
(
  threads: ThreadMXBean = ManagementFactory.getThreadMXBean
) extends Metric with JMXServerSupport {

  override def values: Map[String, AnyVal] = {
    val allThreads = getAllThreads

    val threadsByState: Map[String, Int] =
      Thread.State.values.map(v => ("pool." + v.toString.toLowerCase + ".count", 0)).toMap ++
        allThreads.groupBy(_.getThreadState).map(e => ("pool." +  e._1.toString.toLowerCase + ".count", e._2.size))

    ListMap(
      "count" -> threads.getThreadCount,
      "daemon.count" -> threads.getDaemonThreadCount,
      "deadlock.count" -> Option(threads.findDeadlockedThreads).map(_.length).getOrElse(0)
    ) ++
      threadsByState
  }

  private def getAllThreads: List[ThreadInfo] = threads.getThreadInfo(threads.getAllThreadIds, 0).toList
}
