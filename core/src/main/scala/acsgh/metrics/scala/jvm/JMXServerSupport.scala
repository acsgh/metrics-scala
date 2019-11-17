package acsgh.metrics.scala.jvm

import java.lang.management.ManagementFactory

import javax.management.{Attribute, MBeanServer, ObjectName}

trait JMXServerSupport {

  val mBeanServer: MBeanServer = ManagementFactory.getPlatformMBeanServer

  protected def getOSAttributeLong(attribute: String): Long = getOSAttribute(attribute, classOf[java.lang.Long]).map(_ * 1).getOrElse(0)

  protected def getOSAttributeDouble(attribute: String): Double = getOSAttribute(attribute, classOf[java.lang.Double]).map(value => {
    if (value > 0) {
      value * 1000 / 10.0
    } else {
      value * 1.0
    }
  }).getOrElse(0.0)

  protected def getOSAttribute[T](attribute: String, clazz: Class[T]): Option[T] = {

    val name = ObjectName.getInstance("java.lang:type=OperatingSystem")
    val list = mBeanServer.getAttributes(name, Array[String](attribute))

    if (!list.isEmpty) {
      val value = list.get(0).asInstanceOf[Attribute].getValue

      if (value.getClass == clazz) {
        Some(value.asInstanceOf[T])
      } else {
        None
      }
    } else {
      None
    }
  }
}
