package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.{JsValue, Json, OWrites, Reads}

case class Property
(
  id: Int,
  position: Int,
  unit: Option[Int],
  propertyGroupId: Option[Int],
  backendName: String,
  valueType: String,
  isSearchable: Boolean,
  isOderProperty: Boolean,
  isShownOnItemPage: Boolean,
  isShownOnItemList: Boolean,
  isShownAtCheckout: Boolean,
  isShownInPdf: Boolean,
  comment: String,
  surcharge: Int,
  isShownAsAdditionalCosts: Boolean,
  updatedAt: String
) extends PlentyEntity {
  override def toJson: JsValue = Json.toJson(this)
}

object Property {
  type PropertiesPage = Page[Property]
  implicit val propertyReads: Reads[Property] = Json.reads[Property]
  implicit val propertyWrites: OWrites[Property] = Json.writes[Property]
  implicit val propertiesPageReads: Reads[PropertiesPage] = Page.reads[Property]
}
