package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.{JsValue, Json, OWrites, Reads}

case class Property
(
  id: Int,
  cast: String,
  `type`: String,
  position: Int,
  createdAt: String,
  updatedAt: String,
  names: Option[Array[PropertyName]]
) extends PlentyEntity {
  override def toJson: JsValue = Json.toJson(this)
}

object Property {
  type PropertiesPage = Page[Property]
  implicit val propertyReads: Reads[Property] = Json.reads[Property]
  implicit val propertyWrites: OWrites[Property] = Json.writes[Property]
  implicit val propertiesPageReads: Reads[PropertiesPage] = Page.reads[Property]
}
