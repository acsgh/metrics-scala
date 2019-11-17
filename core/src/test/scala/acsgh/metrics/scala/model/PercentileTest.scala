package acsgh.metrics.scala.model

import org.scalatest._

import scala.collection.immutable.ListMap
import scala.language.reflectiveCalls

class PercentileTest extends FlatSpec with Matchers {

  "Percentile" should "start at 0" in {
    val metric = Percentile()
    metric.values should be(
      ListMap(
        "p-05%" -> 0.0,
        "p-25%" -> 0.0,
        "p-50%" -> 0.0,
        "p-75%" -> 0.0,
        "p-90%" -> 0.0,
        "p-99%" -> 0.0,
        "p-99.5%" -> 0.0
      )
    )
  }

  it should "update" in {
    val metric = Percentile()
    metric.update(1)
    metric.values should be(
      ListMap(
        "p-05%" -> 1.0,
        "p-25%" -> 1.0,
        "p-50%" -> 1.0,
        "p-75%" -> 1.0,
        "p-90%" -> 1.0,
        "p-99%" -> 1.0,
        "p-99.5%" -> 1.0
      )
    )

    (2 to 100).foreach(v => metric.update(v))

    metric.values should be(
      ListMap(
        "p-05%" -> 5.0,
        "p-25%" -> 25.0,
        "p-50%" -> 50.0,
        "p-75%" -> 75.0,
        "p-90%" -> 90.0,
        "p-99%" -> 99.0,
        "p-99.5%" -> 100.0
      )
    )
  }

  it should "reset" in {
    val metric = Percentile()
    metric.update(10)
    metric.reset()
  }

  it should "get value" in {
    val metric = Percentile()
    metric.update(10)
    //    metric.values should be(
    //      ListMap(
    //        "events" -> metric.events,
    //        "total" -> metric.total,
    //        "min" -> metric.min,
    //        "max" -> metric.max,
    //        "mean" -> metric.mean,
    //      )
    //    )
  }
}
