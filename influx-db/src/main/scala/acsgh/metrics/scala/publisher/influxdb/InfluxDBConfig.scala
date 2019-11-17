package acsgh.metrics.scala.publisher.influxdb

import acsgh.metrics.scala.model.PublisherConfig

case class InfluxDBConfig
(
  url: String,
  username: String,
  password: String,
  db: String,
  retentionPolicy: String,
  publishIntervalSecond: Long
) extends PublisherConfig