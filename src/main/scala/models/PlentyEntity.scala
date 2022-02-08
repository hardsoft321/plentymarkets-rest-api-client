package org.hardsoft321.plentymarkets
package models

import play.api.libs.json.JsValue

trait PlentyEntity {
  val id: Int
  def toJson: JsValue
}
