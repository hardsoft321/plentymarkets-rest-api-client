package org.hardsoft321.plentymarkets

import play.api.libs.json.{Json, OWrites, Reads}

object ItemCreateRequest {
  case class ItemUnit
  (
    unitId: Int,
    content: BigDecimal
  )

  case class VariationCategory
  (
    categoryId: Int
  )

  case class Variation
  (
    variationCategories: Seq[VariationCategory],
    unit: ItemUnit
  )

  case class Request
  (
    variations: Seq[Variation]
  )

  implicit val unitWrites: OWrites[ItemUnit] = Json.writes[ItemUnit]
  implicit val categoryWrites: OWrites[VariationCategory] = Json.writes[VariationCategory]
  implicit val variationWrites: OWrites[Variation] = Json.writes[Variation]
  implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  implicit val unitReads: Reads[ItemUnit] = Json.reads[ItemUnit]
  implicit val categoryReads: Reads[VariationCategory] = Json.reads[VariationCategory]
  implicit val variationReads: Reads[Variation] = Json.reads[Variation]
  implicit val requestReads: Reads[Request] = Json.reads[Request]
}
