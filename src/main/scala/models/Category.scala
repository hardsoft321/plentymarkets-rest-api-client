package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.{JsValue, Json, OWrites, Reads}

case class Category
(
  id: Int,
  parentCategoryId: Option[Int],
  level: Int,
  `type`: String,
  linklist: String,
  right: String,
  sitemap: String,
  hasChildren: Boolean,
  details: Array[CategoryDetail]
) extends PlentyEntity {
  override def toJson: JsValue = Json.toJson(this)
}

object Category {
  type CategoriesPage = Page[Category]
  implicit val categoryReads: Reads[Category] = Json.reads[Category]
  implicit val categoryWrites: OWrites[Category] = Json.writes[Category]
  implicit val categoriesPageReads: Reads[CategoriesPage] = Page.reads[Category]
}
