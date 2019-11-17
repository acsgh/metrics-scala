package acsgh.metrics.scala.model

import java.time.Instant

import org.scalatest._

import scala.collection.immutable.ListMap
import scala.language.reflectiveCalls

class RateTest extends FlatSpec with Matchers {

  "Rate" should "start at 0" in {
    val metric = Rate()
    metric.values should be(
      ListMap(
        "rate" -> 0
      )
    )
  }

  it should "update" in {
    var now = Instant.now().minusSeconds(65)

    val metric = Rate(timeProvider = () => now)
    metric.hit(50)
    metric.values should be(
      ListMap(
        "rate" -> 50
      )
    )

    now = now.plusSeconds(65)
    metric.tick()
    metric.hit(10)
    metric.values should be(
      ListMap(
        "rate" -> 10
      )
    )
  }

  it should "reset" in {
    val metric = Rate()
    metric.hit(1)
    metric.reset()
    metric.values should be(
      ListMap(
        "rate" -> 0
      )
    )
  }
}
