package org.hardsoft321.plentymarkets.models

import play.api.libs.json._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import java.time.Instant

case class Contact(
  id: Int,
  externalId: String,
  number: Option[String],
  typeId: Int,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  secondaryEmail: Option[String],
  gender: Option[String],
  title: Option[String],
  formOfAddress: Option[String],
  newsletterAllowanceAt: Option[String],
  classId: Option[Int],
  blocked: Option[Int],
  rating: Option[Int],
  bookAccount: Option[String],
  lang: Option[String],
  referrerId: Option[Double],
  plentyId: Option[Int],
  userId: Option[Int],
  birthdayAt: Option[String],
  lastLoginAt: Option[String],
  lastLoginAtTimestamp: Option[String],
  lastOrderAt: Option[String],
  createdAt: Instant,
  updatedAt: Instant,
  privatePhone: Option[String],
  privateFax: Option[String],
  privateMobile: Option[String],
  ebayName: Option[String],
  paypalEmail: Option[String],
  paypalPayerId: Option[String],
  klarnaPersonalId: Option[String],
  dhlPostIdent: Option[String],
  forumUsername: Option[String],
  forumGroupId: Option[String],
  singleAccess: Option[String],
  contactPerson: Option[String],
  marketplacePartner: Option[String],
  valuta: Option[Int],
  discountDays: Option[Int],
  discountPercent: Option[BigDecimal],
  timeForPaymentAllowedDays: Option[Int],
  salesRepresentativeContactId: Option[Int],
  anonymizeAt: Option[String],
  isLead: Option[Boolean],
  leadStatusKey: Option[String],
  inLeadStatusSince: Option[Int],
  leadStatusUpdateAt: Option[String],
)

object Contact {
  implicit val contactFormat: Format[Contact] = Jsonx.formatCaseClass[Contact]
}
