package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.{JsValue, Json, OWrites, Reads}

case class Attribute
(
  id: Int,
  backendName: String,
  position: Int,
  isSurchargePercental: Boolean,
  isLinkableToImage: Boolean,
  amazonAttribute: String,
  fruugoAttribute: String,
  pixmaniaAttribute: Int,
  ottoAttribute: String,
  googleShoppingAttribute: String,
  neckermannAtEpAttribute: Int,
  typeOfSelectionInOnlineStore: String,
  laRedouteAttribute: Int,
  isGroupable: Boolean,
  updatedAt: String // @todo: Choose good date format for this field "2021-11-23T11:52:17+01:00"
) extends PlentyEntity {
  override def toJson: JsValue = Json.toJson(this)
}

object Attribute {
  type AttributesPage = Page[Attribute]
  implicit val attributeReads: Reads[Attribute] = Json.reads[Attribute]
  implicit val attributeWrites: OWrites[Attribute] = Json.writes[Attribute]
  implicit val attributesPageReads: Reads[AttributesPage] = Page.reads[Attribute]
}
