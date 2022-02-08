package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.{JsValue, Json, OWrites, Reads}

case class AttributeValue
(
  id: Int,
  attributeId: Int,
  backendName: String,
  position: Int,
  image: String,
  comment: String,
  amazonValue: String,
  ottoValue: String,
  neckermannAtEpValue: String,
  laRedouteValue: String,
  tracdelightValue: String,
  percentageDistribution: Int,
  updatedAt: String
) extends PlentyEntity {
  override def toJson: JsValue = Json.toJson(this)
}

object AttributeValue {
  type AttributeValuesPage = Page[AttributeValue]
  implicit val attributeValueReads: Reads[AttributeValue] = Json.reads[AttributeValue]
  implicit val attributeValueWrites: OWrites[AttributeValue] = Json.writes[AttributeValue]
  implicit val attributeValuesPageReasds: Reads[AttributeValuesPage] = Page.reads[AttributeValue]
}
