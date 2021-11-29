package org.hardsoft321.plentymarkets

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class OAuthToken
(
  tokenType: String,
  expiresIn: Int,
  accessToken: String,
  refreshToken: String,
  userId: Int,
  concurrentSessions: Int,
  maxConcurrentSessions: Int
) {
  def expired(): Boolean = (expiresIn - (System.currentTimeMillis() - createdAt) / 1000) <= 0L

  val createdAt: Long = System.currentTimeMillis()
}

object OAuthTokenImplicits {
  implicit val OAuthResponseReads: Reads[OAuthToken] = (
    (JsPath \ "token_type").read[String] and
      (JsPath \ "expires_in").read[Int] and
      (JsPath \ "access_token").read[String] and
      (JsPath \ "refresh_token").read[String] and
      (JsPath \ "user_id").read[Int] and
      (JsPath \ "concurrent_sessions").read[Int] and
      (JsPath \ "max_current_sessions").read[Int]
    )(OAuthToken.apply _)
}