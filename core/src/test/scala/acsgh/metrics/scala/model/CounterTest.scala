package acsgh.metrics.scala.model

import org.scalatest._

import scala.language.reflectiveCalls

class CounterTest extends FlatSpec with Matchers {

  "Counter" should "start at 0" in {
    val metric = Counter()
    metric.values should be(
      Map(
        "count" -> 0
      )
    )
  }

  it should "increase" in {
    val metric = Counter()
    metric.inc()
    metric.values should be(
      Map(
        "count" -> 1
      )
    )
  }

  it should "decrease" in {
    val metric = Counter()
    metric.dec()
    metric.values should be(
      Map(
        "count" -> -1
      )
    )
  }

  it should "reset" in {
    val metric = Counter()
    metric.inc()
    metric.reset()
    metric.values should be(
      Map(
        "count" -> 0
      )
    )
  }

  it should "get value" in {
    val metric = Counter()
    metric.inc()
    metric.values should be(
      Map(
        "count" -> metric.count
      )
    )
  }
}
