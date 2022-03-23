package org.hardsoft321.plentymarkets.models

import play.api.libs.json._
import java.time.Instant

case class OrderReference(
  id: Int,
  orderId: String,
  originOrderId: String,
  referenceOrderId: String,
  referenceType: String,
  createdAt: Instant,
  updatedAt: Instant,
)

object OrderReference {
  implicit val orderReferenceFormat: Format[OrderReference] = Json.format[OrderReference]
}
