/*
 * Copyright Hardsoft321, Ltd.
 * Licensed under Apache-2.0
 * Author Timur Rakhimzhanov <rtr@lab321.ru>
 */

package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.{JsValue, Json, OWrites, Reads}

case class PropertyName
(
  id: Int,
  propertyId: Int,
  lang: String,
  name: String,
  description: String,
  createdAt: String,
  updatedAt: String
) extends PlentyEntity {
  override def toJson: JsValue = Json.toJson(this)
}

object PropertyName {
  type PropertyNamesPage = Page[PropertyName]
  implicit val propertyNameReads: Reads[PropertyName] = Json.reads[PropertyName]
  implicit val propertyNameWrites: OWrites[PropertyName] = Json.writes[PropertyName]
  implicit val propertyNamesPageReads: Reads[PropertyNamesPage] = Page.reads[PropertyName]
}
