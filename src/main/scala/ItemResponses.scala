package org.hardsoft321.plentymarkets

import play.api.libs.json.{Json, OWrites, Reads}

object ItemResponses {
  sealed trait Entity {
    val id: Int
  }
  sealed trait Response
  sealed trait ErrorResponse extends Response
  type PlentyResponse[A <: Response, B <: Response] =
    Either[A, B]

  /**
   * Base class for all entries that have name field
   *
   * @param name Name of entity
   * @tparam NameType Type of name field. Could be any type (String, Int, etc)
   */
  sealed abstract class EntityWithName[NameType](name: NameType) extends Entity {
    def getName: NameType = name
  }

  case class Attribute
  (
    id: Int,
    backendName: String,
    position: Int,
    isSurchargePercental: Boolean,
    isLinkableToImage: Boolean,
    amazonAttribute: String,
    fruugoAttribute: String,
    pixmaniaAttribute: Int,
    ottoAttribute: String,
    googleShoppingAttribute: String,
    neckermannAtEpAttribute: Int,
    typeOfSelectionInOnlineStore: String,
    laRedouteAttribute: Int,
    isGroupable: Boolean,
    updatedAt: String // @todo: Choose good date format for this field "2021-11-23T11:52:17+01:00"
  ) extends EntityWithName(backendName)

  implicit val attributeReads: Reads[Attribute] = Json.reads[Attribute]
  implicit val attributeWrites: OWrites[Attribute] = Json.writes[Attribute]

  /**
   * Variation attribute value entity represantation
   */
  case class AttributeValue
  (
    id: Int,
    attributeId: Int,
    backendName: String,
    position: Int,
    image: String,
    comment: String,
    amazonValue: String,
    ottoValue: String,
    neckermannAtEpValue: String,
    laRedouteValue: String,
    tracdelightValue: String,
    percentageDistribution: Int,
    updatedAt: String
  ) extends EntityWithName(backendName) with Response

  implicit val attributeValueReads: Reads[AttributeValue] = Json.reads[AttributeValue]
  implicit val attributeValueWrites: OWrites[AttributeValue] = Json.writes[AttributeValue]

  /**
   * Manufacturer entity representation
   */
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
  ) extends EntityWithName(name) with Response

  implicit val manufacturerReads: Reads[Manufacturer] = Json.reads[Manufacturer]
  implicit val manufacturerWrites: OWrites[Manufacturer] = Json.writes[Manufacturer]

  case class Variation
  (
    id: Int,
    isMain: Boolean,
    mainVariationId: Option[Int],
    itemId: Int,
    position: Option[Int],
    isActive: Boolean,
    number: String
  ) extends Response with Entity
  implicit val variationReads: Reads[Variation] = Json.reads[Variation]
  implicit val variationWrites: OWrites[Variation] = Json.writes[Variation]

  /**
   * Base class for pages Responses
   *
   * @tparam EntityType response entity
   * @tparam ItemsPerPageType There are some routes that have different itemsPerPage type
   */
  case class Page[EntityType, ItemsPerPageType]
  (
    page: Int,
    totalsCount: Int,
    isLastPage: Boolean,
    lastPageNumber: Int,
    firstOnPage: Int,
    lastOnPage: Int,
    itemsPerPage: ItemsPerPageType,
    entries: Array[EntityType]
  ) extends Response

  type AttributesPage = Page[Attribute, Int]
  type AttributeValuesPage = Page[AttributeValue, Int]
  type ManufacturersPage = Page[Manufacturer, String]
  type VariationsPage = Page[Variation, Int]

  implicit val manufacturerPageReads: Reads[ManufacturersPage] = Json.reads[ManufacturersPage]
  implicit val attributeValuesPageReads: Reads[AttributeValuesPage] = Json.reads[AttributeValuesPage]
  implicit val attributesPageReads: Reads[AttributesPage] = Json.reads[AttributesPage]
  implicit val variationsPageReads: Reads[VariationsPage] = Json.reads[VariationsPage]

  case class ValidationError(message: String)
  implicit val validationErrorReads: Reads[ValidationError] = Json.reads[ValidationError]

  case class ValidationErrorResponse
  (
    error: ValidationError,
    validation_errors: Map[String, Array[String]]
  ) extends Response
  implicit val validationErrorResponseReads: Reads[ValidationErrorResponse] = Json.reads[ValidationErrorResponse]

  case class Item
  (
    id: Int,
    manufacturerId: Int,
    variations: Array[Variation]
  ) extends Response
  implicit val itemReads: Reads[Item] = Json.reads[Item]

  case class WarehouseStock
  (
    purchasePrice: Double,
    reservedListing: Int,
    reservedBundles: Int,
    variationId: Int,
    itemId: Int,
    warehouseId: Int,
    physicalStock: Int,
    reservedStock: Int,
    netStock: Int,
    reorderLevel: Int,
    deltaReorderLevel: Int,
    valueOfGoods: Option[Double]
  ) extends Response
  implicit val warehouseStockReads: Reads[WarehouseStock] = Json.reads[WarehouseStock]

  type WarehouseStockResponse = Array[WarehouseStock]
  //implicit val warehouseStockResponseReads: Reads[WarehouseStockResponse] = Json.reads[WarehouseStockResponse]
}
