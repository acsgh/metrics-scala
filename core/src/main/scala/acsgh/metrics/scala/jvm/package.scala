package acsgh.metrics.scala

package object jvm {
  private val JVM = "jvm"
  private val MEMORY = JVM + ".mem"
  private val CLASSLOADER = JVM + ".class"
  private val GC = JVM + ".gc"
  private val THREAD = JVM + ".thread"
  private val CPU = JVM + ".cpu"

  val DEFAULT_METRICS = Map(
    GC -> GarbageCollectorMetrics(),
    MEMORY -> MemoryMetrics(),
    CLASSLOADER -> ClassLoadingMetrics(),
    THREAD -> ThreadMetrics(),
    CPU -> CPUMetrics()
  )
}
