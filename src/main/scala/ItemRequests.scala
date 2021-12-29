package org.hardsoft321.plentymarkets

import play.api.libs.json.{Format, Json, OWrites, Reads}

object ItemRequests {

  sealed trait Request

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
  ) extends Request

  implicit val attributeValueFormat: Format[AttributeValue] = Json.format[AttributeValue]

  case class ItemText
  (
    name: String,
    lang: String = "en"
  )

  implicit val textWrites: OWrites[ItemText] = Json.writes[ItemText]
  implicit val textReads: Reads[ItemText] = Json.reads[ItemText]

  case class VariationUnit
  (
    unitId: Int,
    content: Int = 1
  )

  implicit val unitWrites: OWrites[VariationUnit] = Json.writes[VariationUnit]
  implicit val unitReads: Reads[VariationUnit] = Json.reads[VariationUnit]

  case class VariationWarehouse
  (
    warehouseId: Int,
    reorderLevel: Int = 1
  )

  implicit val warehouseWrites: OWrites[VariationWarehouse] = Json.writes[VariationWarehouse]
  implicit val warehouseReads: Reads[VariationWarehouse] = Json.reads[VariationWarehouse]

  case class VariationSalesPrice
  (
    salesPriceId: Int,
    price: Float
  )

  implicit val salesPriceWrites: OWrites[VariationSalesPrice] = Json.writes[VariationSalesPrice]
  implicit val salesPriceReads: Reads[VariationSalesPrice] = Json.reads[VariationSalesPrice]

  case class VariationBarcode
  (
    code: String,
    barcodeId: Int
  )

  implicit val barcodeWrites: OWrites[VariationBarcode] = Json.writes[VariationBarcode]
  implicit val barcodeReads: Reads[VariationBarcode] = Json.reads[VariationBarcode]

  case class VariationPlentyClient
  (
    plentyId: Int //57181
  )

  implicit val plentyClientWrites: OWrites[VariationPlentyClient] = Json.writes[VariationPlentyClient]
  implicit val plentyClientReads: Reads[VariationPlentyClient] = Json.reads[VariationPlentyClient]

  case class VariationAttributeValue
  (
    valueId: Int
  )

  implicit val variationAttributeValueWrites: OWrites[VariationAttributeValue] = Json.writes[VariationAttributeValue]
  implicit val variationAttributeValueReads: Reads[VariationAttributeValue] = Json.reads[VariationAttributeValue]

  case class VariationCategory
  (
    categoryId: Int
  )
  implicit val variationCategoryWrites: OWrites[VariationCategory] = Json.writes[VariationCategory]
  implicit val variationCategoryReads: Reads[VariationCategory] = Json.reads[VariationCategory]

  case class ItemVariation
  (
    variationCategories: Array[VariationCategory],
    unit: VariationUnit,
    name: Option[String] = None,
    model: Option[String] = None,
    number: Option[String] = None,
    mainWarehouseId: Option[Int] = None,
    variationAttributeValues: Option[Array[VariationAttributeValue]] = None,
    variationClients: Option[Array[VariationPlentyClient]] = None,
    variationBarcodes: Option[Array[VariationBarcode]] = None,
    variationSalesPrices: Option[Array[VariationSalesPrice]] = None,
    variationWarehouses: Option[Array[VariationWarehouse]] = None,
    isActive: Boolean = true
  )
  implicit val itemVariationWrites: OWrites[ItemVariation] = Json.writes[ItemVariation]
  implicit val itemVariationReads: Reads[ItemVariation] = Json.reads[ItemVariation]

  case class Item
  (
    variations: Array[ItemVariation],
    texts: Option[Array[ItemText]] = None,
    manufacturerId: Option[Int] = None
  )

  implicit val itemWrites: OWrites[Item] = Json.writes[Item]
  implicit val itemReads: Reads[Item] = Json.reads[Item]

  object ItemRequest {
    def apply(items: Item*): Array[Item] = Array(items: _*)
  }

  case class BookIncomingStockRequest
  (
    warehouseId: Int,
    deliveredAt: String,
    currency: String,
    quantity: Int,
    purchasePrice: Float,
    reasonId: Int
  )

  implicit val bookIncomingStockRequestWrites: OWrites[BookIncomingStockRequest] = Json.writes[BookIncomingStockRequest]
  implicit val bookIncomingStockRequestReads: Reads[BookIncomingStockRequest] = Json.reads[BookIncomingStockRequest]

  case class StockCorrectionRequest
  (
    quantity: Int,
    warehouseId: Int,
    storageLocationId: Int,
    reasonId: Int
  )

  implicit val stockCorrectionRequestWrites: OWrites[StockCorrectionRequest] = Json.writes[StockCorrectionRequest]
  implicit val stockCorrectionRequestReads: Reads[StockCorrectionRequest] = Json.reads[StockCorrectionRequest]

  /*implicit val itemsRequestWrites: OWrites[Array[Item]] = Json.writes[Array[Item]]
  implicit val itemsRequestReads: Reads[Array[Item]] = Json.reads[Array[Item]]*/
}
