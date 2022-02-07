package org.hardsoft321.plentymarkets.models

import play.api.libs.json._
import java.time.Instant

case class OrderItem(
  id: Int,
  orderId: Int,
  typeId: Option[Int],
  referrerId: Option[Double],
  itemVariationId: Option[Int],
  quantity: Option[Double],
  orderItemName: Option[String],
  attributeValues: Option[String],
  shippingProfileId: Option[Int],
  countryVatId: Option[Int],
  vatField: Option[Int],
  vatRate: Option[Double],
  position: Option[String], // Int in doc
  warehouseId: Option[Int],
  createdAt: Instant,
  updatedAt: Instant,
  amounts: Array[OrderItemAmount],
  variationBarcodes: Option[Array[VariationBarcode]],
)

object OrderItem {
  implicit val orderItemReads: Reads[OrderItem] = Json.reads[OrderItem]
  implicit val orderItemWrites: OWrites[OrderItem] = Json.writes[OrderItem]
}
