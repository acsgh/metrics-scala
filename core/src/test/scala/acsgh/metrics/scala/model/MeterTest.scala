package acsgh.metrics.scala.model

import org.scalatest._

import scala.collection.immutable.ListMap
import scala.language.reflectiveCalls

class MeterTest extends FlatSpec with Matchers {

  "Meter" should "start at 0" in {
    val metric = Meter()

    metric.values should be(
      ListMap(
        "events" -> 0,
        "total" -> 0.0,
        "min" -> 0.0,
        "max" -> 0.0,
        "mean" -> 0.0,
        "rate" -> 0
      )
    )
  }

  it should "update" in {
    val metric = Meter()
    metric.hit(10)
    Thread.sleep(2000)
    metric.values should be(
      ListMap(
        "events" -> 1,
        "total" -> 10.0,
        "min" -> 10.0,
        "max" -> 10.0,
        "mean" -> 10.0,
        "rate" -> 10
      )
    )
  }

  it should "reset" in {
    val metric = Meter()
    metric.hit(1000)
    metric.reset()
    metric.values should be(
      ListMap(
        "events" -> 0,
        "total" -> 0.0,
        "min" -> 0.0,
        "max" -> 0.0,
        "mean" -> 0.0,
        "rate" -> 0
      )
    )
  }
}
