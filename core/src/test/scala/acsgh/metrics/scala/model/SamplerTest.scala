package acsgh.metrics.scala.model

import org.scalatest._

import scala.collection.immutable.ListMap
import scala.language.reflectiveCalls

class SamplerTest extends FlatSpec with Matchers {

  "Sampler" should "start at 0" in {
    val metric = Sampler()
    metric.events should be(0)
    metric.total should be(0)
    metric.min should be(0)
    metric.max should be(0)
    metric.mean should be(0)
  }

  it should "update" in {
    val metric = Sampler()
    metric.events should be(0)
    metric.total should be(0)
    metric.min should be(0)
    metric.max should be(0)
    metric.mean should be(0)
    metric.update(10)
    metric.events should be(1)
    metric.total should be(10)
    metric.min should be(10)
    metric.max should be(10)
    metric.mean should be(10)
    metric.update(20)
    metric.events should be(2)
    metric.total should be(30)
    metric.min should be(10)
    metric.max should be(20)
    metric.mean should be(15)
  }

  it should "reset" in {
    val metric = Sampler()
    metric.update(10)
    metric.reset()
    metric.events should be(0)
    metric.total should be(0)
    metric.min should be(0)
    metric.max should be(0)
    metric.mean should be(0)
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
