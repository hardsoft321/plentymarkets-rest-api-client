package org.hardsoft321.plentymarkets.models

import play.api.libs.json._
import java.time.Instant

case class VariationBarcode
(
  barcodeId: Int,
  variationId: Int,
  code: String,
  createdAt: Instant,
)

object VariationBarcode {
  implicit val variationBarcodeReads: Reads[VariationBarcode] = Json.reads[VariationBarcode]
  implicit val variationBarcodeWrites: OWrites[VariationBarcode] = Json.writes[VariationBarcode]
}
