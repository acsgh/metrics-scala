package acsgh.metrics.scala.model

import org.scalatest._

import scala.collection.immutable.ListMap
import scala.language.reflectiveCalls

class SamplerTest extends FlatSpec with Matchers {

  "Sampler" should "start at 0" in {
    val metric = Sampler()
    metric.values should be(
      ListMap(
        "events" -> 0,
        "total" -> 0.0,
        "min" -> 0.0,
        "max" -> 0.0,
        "mean" -> 0.0
      )
    )
  }

  it should "update" in {
    val metric = Sampler()

    metric.values should be(
      ListMap(
        "events" -> 0,
        "total" -> 0.0,
        "min" -> 0.0,
        "max" -> 0.0,
        "mean" -> 0.0
      )
    )
    metric.update(10)
    metric.values should be(
      ListMap(
        "events" -> 1,
        "total" -> 10.0,
        "min" -> 10.0,
        "max" -> 10.0,
        "mean" -> 10.0
      )
    )
    metric.update(20)
    metric.values should be(
      ListMap(
        "events" -> 2,
        "total" -> 30.0,
        "min" -> 10.0,
        "max" -> 20.0,
        "mean" -> 15.0
      )
    )
  }

  it should "reset" in {
    val metric = Sampler()
    metric.update(10)
    metric.reset()
    metric.values should be(
      ListMap(
        "events" -> 0,
        "total" -> 0.0,
        "min" -> 0.0,
        "max" -> 0.0,
        "mean" -> 0.0
      )
    )
  }

  it should "get value" in {
    val metric = Sampler()
    metric.update(10)
    metric.values should be(
      ListMap(
        "events" -> metric.events,
        "total" -> metric.total,
        "min" -> metric.min,
        "max" -> metric.max,
        "mean" -> metric.mean,
      )
    )
  }
}
