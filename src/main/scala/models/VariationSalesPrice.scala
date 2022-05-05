package org.hardsoft321.plentymarkets.models

import play.api.libs.json._
import java.time.Instant

case class VariationSalesPrice(
  variationId: Int,
  salesPriceId: Int,
  price: BigDecimal,
  createdAt: Instant,
  updatedAt: Instant,
)

object VariationSalesPrice {
  implicit val variationSalesPriceReads: Reads[VariationSalesPrice] = Json.reads[VariationSalesPrice]
  implicit val variationSalesPriceWrites: OWrites[VariationSalesPrice] = Json.writes[VariationSalesPrice]
}
