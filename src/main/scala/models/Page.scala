package org.hardsoft321.plentymarkets.models

import play.api.libs.json._

case class Page[Entry](
  page: Int,
  totalsCount: Int,
  isLastPage: Boolean,
  lastPageNumber: Int,
  firstOnPage: Int,
  lastOnPage: Int,
  itemsPerPage: Int,
  entries: IndexedSeq[Entry],
)

object Page {
  implicit val jsonPageReads: Reads[Page[JsValue]] = reads[JsValue]

  def reads[Entry](implicit r: Reads[Entry]): Reads[Page[Entry]] = Reads(jsValue =>
    for {
      page <- (jsValue \ "page").validate[Int]
      totalsCount <- (jsValue \ "totalsCount").validate[Int]
      isLastPage <- (jsValue \ "isLastPage").validate[Boolean]
      lastPageNumber <- (jsValue \ "lastPageNumber").validate[Int]
      firstOnPage <- (jsValue \ "firstOnPage").validate[Int]
      lastOnPage <- (jsValue \ "lastOnPage").validate[Int]
      itemsPerPage <- (jsValue \ "itemsPerPage").validate[Int].orElse(
        (jsValue \ "itemsPerPage").validate[String].flatMap(
          _.toIntOption.map(JsSuccess(_)).getOrElse(JsError(__ \ "itemsPerPage", "Not integer value"))))
      entries <- (jsValue \ "entries").validate[IndexedSeq[Entry]]
    } yield Page(
      page = page,
      totalsCount = totalsCount,
      isLastPage = isLastPage,
      lastPageNumber = lastPageNumber,
      firstOnPage = firstOnPage,
      lastOnPage = lastOnPage,
      itemsPerPage = itemsPerPage,
      entries = entries,
    )
  )
}
