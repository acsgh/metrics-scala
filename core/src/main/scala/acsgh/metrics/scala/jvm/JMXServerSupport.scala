package acsgh.metrics.scala.jvm

import java.lang.management.ManagementFactory

import javax.management.{Attribute, MBeanServer, ObjectName}

trait JMXServerSupport {

  val mBeanServer: MBeanServer = ManagementFactory.getPlatformMBeanServer

  protected def getOSAttributeLong(attribute: String): Long = getOSAttribute[Long](attribute).getOrElse(0)

  protected def getOSAttributeDouble(attribute: String): Double = getOSAttribute[Double](attribute).map(value => {
    if (value > 0) {
      value * 1000 / 10.0
    } else {
      value
    }
  }).getOrElse(0.0)

  protected def getOSAttribute[T <: AnyVal](attribute: String): Option[T] = {

    val name = ObjectName.getInstance("java.lang:type=OperatingSystem")
    val list = mBeanServer.getAttributes(name, Array[String](attribute))

    if (!list.isEmpty) {
      val value = list.get(0).asInstanceOf[Attribute].getValue
      value match {
        case t: T =>
          Some(t)
        case _ =>
          None
      }
    } else {
      None
    }
  }
}
