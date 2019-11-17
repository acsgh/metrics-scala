package acsgh.metrics.scala.publisher.influxdb

import acsgh.metrics.scala.MetricsRegistry

object Boot extends App{

  val registry = MetricsRegistry.newInstance()
  registry.start()
registry.snapshot.foreach(println(_))
}
