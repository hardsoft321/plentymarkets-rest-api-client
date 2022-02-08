package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.{Json, OWrites, Reads}

case class CategoryDetail
(
  categoryId: Int,
  lang: String,
  name: String,
  description: String,
  description2: String,
  shortDescription: String,
  metaKeywords: String,
  metaDescription: String,
  nameUrl: String,
  metaTitle: String,
  position: String,
  updatedAt: String,
  updatedBy: String,
  //itemListView: String,
  //singleItemView: String,
  //pageView: String,
  fulltext: String,
  metaRobots: String,
  canonicalLink: String,
  previewUrl: String,
  image: Option[String],
  imagePath: Option[String],
  image2: Option[String],
  image2Path: Option[String],
  plentyId: Int
)

object CategoryDetail {
  implicit val categoryDetailReads: Reads[CategoryDetail] = Json.reads[CategoryDetail]
  implicit val categoryDetailWrites: OWrites[CategoryDetail] = Json.writes[CategoryDetail]
}
