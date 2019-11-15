package acsgh.metrics.scala.model

import java.util.concurrent.TimeUnit

import org.scalatest._

import scala.language.reflectiveCalls

class TimerTest extends FlatSpec with Matchers {

  "Metric" should "start at 0" in {
    val metric = Timer()

    metric.values should be(
      Map(
        "total" -> 0,
        "min" -> 0,
        "mean" -> 0.0,
        "max" -> 0,
        "std-dev" -> 0.0,
        "p05" -> 0,
        "p25" -> 0,
        "p50" -> 0,
        "p75" -> 0,
        "p90" -> 0,
        "p99" -> 0,
        "p995" -> 0,
        "events" -> 0,
        "mean-rate" -> 0.0,
        "1m-rate" -> 0.0,
        "5m-rate" -> 0.0,
        "15m-rate" -> 0.0
      )
    )
  }

  it should "fail if smaller than millisecond" in {
    val metric = Timer()
    a[IllegalArgumentException] should be thrownBy {
      metric.update(1, TimeUnit.MICROSECONDS)
      fail("Should not be here")
    }
  }

  it should "update" in {
    val metric = Timer()
    metric.update(1, TimeUnit.SECONDS)
    Thread.sleep(2000)
    metric.values should be(
      Map(
        "total" -> 1000,
        "min" -> 1000,
        "mean" -> 1000.0,
        "max" -> 1000,
        "std-dev" -> 0.0,
        "p05" -> 1000,
        "p25" -> 1000,
        "p50" -> 1000,
        "p75" -> 1000,
        "p90" -> 1000,
        "p99" -> 1000,
        "p995" -> 1000,
        "events" -> 1,
        "mean-rate" -> 1.0,
        "1m-rate" -> 1.0,
        "5m-rate" -> 1.0,
        "15m-rate" -> 1.0
      )
    )
  }

  it should "reset" in {
    val metric = Timer()
    metric.update(1, TimeUnit.SECONDS)
    metric.reset()
    metric.values should be(
      Map(
        "total" -> 0,
        "min" -> 0,
        "mean" -> 0.0,
        "max" -> 0,
        "std-dev" -> 0.0,
        "p05" -> 0,
        "p25" -> 0,
        "p50" -> 0,
        "p75" -> 0,
        "p90" -> 0,
        "p99" -> 0,
        "p995" -> 0,
        "events" -> 0,
        "mean-rate" -> 0.0,
        "1m-rate" -> 0.0,
        "5m-rate" -> 0.0,
        "15m-rate" -> 0.0
      )
    )
  }
}
