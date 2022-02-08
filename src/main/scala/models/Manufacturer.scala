package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.{JsValue, Json, OWrites, Reads}

case class Manufacturer
(
  id: Int,
  name: String,
  logo: Option[String],
  url: Option[String],
  pixmaniaBrandId: Option[Int],
  neckermannBrandId: Option[Int],
  externalName: Option[String],
  neckermannAtEpBrandId: Option[Int],
  street: Option[String],
  houseNo: Option[String],
  postcode: Option[String],
  town: Option[String],
  countryId: Option[Int],
  phoneNumber: Option[String],
  faxNumber: Option[String],
  email: Option[String],
  laRedouteBrandId: Option[Int],
  comment: Option[String],
  updatedAt: Option[String],
  position: Option[Int]
) extends PlentyEntity {
  override def toJson: JsValue = Json.toJson(this)
}

object Manufacturer {
  type ManufacturersPage = Page[Manufacturer]
  implicit val manufacturerReads: Reads[Manufacturer] = Json.reads[Manufacturer]
  implicit val manufacturerWrites: OWrites[Manufacturer] = Json.writes[Manufacturer]
  implicit val manufacturersPageReads: Reads[ManufacturersPage] = Page.reads[Manufacturer]
}
