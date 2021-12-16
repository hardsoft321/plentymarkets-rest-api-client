package org.hardsoft321.plentymarkets

import play.api.libs.json.{Format, Json}

object ItemRequests {

  case class AttributeValue
  (
    backendName: String,
    id: Option[Int] = None,
    attributeId: Option[Int] = None,
    position: Option[Int] = None,
    image: Option[String] = None,
    comment: Option[String] = None,
    amazonValue: Option[String] = None,
    ottoValue: Option[String] = None,
    neckermannAtEpValue: Option[String] = None,
    laRedouteValue: Option[String] = None,
    tracdelightValue: Option[String] = None,
    percentageDistribution: Option[Int] = None,
    updatedAt: Option[String] = None
  )

  implicit val attributeValueFormat: Format[AttributeValue] = Json.format[AttributeValue]

}
