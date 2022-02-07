package org.hardsoft321.plentymarkets.models

import play.api.libs.json._
import java.time.Instant

case class Order(
  id: Int,
  typeId: Option[Int],
  statusId: Option[Double],
  statusName: Option[String],
  ownerId: Option[String], // Int in doc
  referrerId: Option[Double],
  createdAt: Instant,
  updatedAt: Instant,
  plentyId: Option[Int],
  locationId: Option[String], // Int in doc
  roundTotalsOnly: Option[Boolean],
  numberOfDecimals: Option[Int],
  lockStatus: Option[String],
  isLocked: Option[Boolean],
  hasTaxRelevantDocuments: Option[Boolean],
  hasDeliveryOrders: Option[Boolean],
  legacyOrderType: Option[String],
  contactSenderId: Option[Int],
  contactReceiverId: Option[Int],
  warehouseSenderId: Option[Int],
  warehouseReceiverId: Option[Int],
  orderItems: Seq[OrderItem],
)

object Order {
  implicit val orderReads: Reads[Order] = Json.reads[Order]
  implicit val orderWrites: OWrites[Order] = Json.writes[Order]
}
