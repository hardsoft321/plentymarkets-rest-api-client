package org.hardsoft321.plentymarkets.models

import play.api.libs.json._
import java.time.Instant

case class Document(
  id: Int,
  `type`: String,
  number: Option[String], // Int in doc
  numberWithPrefix: Option[String],
  path: Option[String],
  userId: Option[String], // Int in doc
  source: String,
  createdAt: Instant,
  updatedAt: Instant,
  displayDate: Instant,
  status: Option[String],
)

object Document {
  implicit val documentFormat: Format[Document] = Json.format[Document]
}
