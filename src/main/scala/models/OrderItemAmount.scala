package org.hardsoft321.plentymarkets.models

import play.api.libs.json._
import java.time.Instant

case class OrderItemAmount(
  id: Int,
  orderItemId: Int,
  isSystemCurrency: Boolean,
  currency: String,
  exchangeRate: BigDecimal,
  purchasePrice: BigDecimal,
  priceOriginalGross: BigDecimal,
  priceOriginalNet: BigDecimal,
  priceGross: BigDecimal,
  priceNet: BigDecimal,
  surcharge: BigDecimal,
  discount: BigDecimal,
  isPercentage: Boolean,
  createdAt: Instant,
  updatedAt: Instant,
)

object OrderItemAmount {
  implicit val orderItemAmountReads: Reads[OrderItemAmount] = Json.reads[OrderItemAmount]
  implicit val orderItemAmountWrites: OWrites[OrderItemAmount] = Json.writes[OrderItemAmount]
}
