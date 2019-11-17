package acsgh.metrics.scala.model

import java.util.concurrent.TimeUnit

import org.scalatest._

import scala.collection.immutable.ListMap
import scala.language.reflectiveCalls

class TimerTest extends FlatSpec with Matchers {

  "Timer" should "start at 0" in {
    val metric = Timer()

    metric.values should be(
      ListMap(
        "events" -> 0,
        "total" -> 0.0,
        "min" -> 0.0,
        "max" -> 0.0,
        "mean" -> 0.0,
        "p-05%" -> 0.0,
        "p-25%" -> 0.0,
        "p-50%" -> 0.0,
        "p-75%" -> 0.0,
        "p-90%" -> 0.0,
        "p-99%" -> 0.0,
        "p-99.5%" -> 0.0,
        "rate" -> 0.0
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
      ListMap(
        "events" -> 1,
        "total" -> 1000.0,
        "min" -> 1000.0,
        "max" -> 1000.0,
        "mean" -> 1000.0,
        "p-05%" -> 1000.0,
        "p-25%" -> 1000.0,
        "p-50%" -> 1000.0,
        "p-75%" -> 1000.0,
        "p-90%" -> 1000.0,
        "p-99%" -> 1000.0,
        "p-99.5%" -> 1000.0,
        "rate" -> 1
      )
    )
  }

  it should "reset" in {
    val metric = Timer()
    metric.update(1, TimeUnit.SECONDS)
    metric.reset()
    metric.values should be(
      Map(
        "events" -> 0,
        "total" -> 0.0,
        "min" -> 0.0,
        "max" -> 0.0,
        "mean" -> 0.0,
        "p-05%" -> 0.0,
        "p-25%" -> 0.0,
        "p-50%" -> 0.0,
        "p-75%" -> 0.0,
        "p-90%" -> 0.0,
        "p-99%" -> 0.0,
        "p-99.5%" -> 0.0,
        "rate" -> 0
      )
    )
  }
}
