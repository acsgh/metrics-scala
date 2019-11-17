package acsgh.metrics.scala.model

import enumeratum._

sealed trait PublishStrategy extends EnumEntry

object PublishStrategy extends Enum[PublishStrategy] {

  val values = findValues

  case object Nothing extends PublishStrategy

  case object Reset extends PublishStrategy

  case object Clean extends PublishStrategy

}

