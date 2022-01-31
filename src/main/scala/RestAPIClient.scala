package org.hardsoft321.plentymarkets

import ItemRequests.{BookIncomingStockRequest, Item, ItemRequest, ItemVariation, StockCorrectionRequest, AttributeValue => AttributeValueRequest}
import ItemResponses.{PlentyResponse, Response, ValidationErrorResponse, VariationsPage, AttributeValue => AttributeValueResponse}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json._

import java.lang.{System => JavaSystem}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

object RestAPIClient extends StrictLogging {
  private val strictTimeout = 10.seconds
  sealed trait OperationType
  case class WriteOperation() extends OperationType
  case class ReadOperation() extends OperationType

  case class RequestLimits
  (
    `x-plenty-global-long-period-limit`: Int,
    `x-plenty-global-long-period-decay`: Int,
    `x-plenty-global-long-period-calls-left`: Int,
    `x-plenty-global-short-period-limit`: Int,
    `x-plenty-global-short-period-decay`: Int,
    `x-plenty-global-short-period-calls-left`: Int
  ) {
    val updated: Long = JavaSystem.currentTimeMillis()
    def secondsPassed: Int = ((JavaSystem.currentTimeMillis() - updated) / 1000).toInt
    def limitReached: Boolean =
      `x-plenty-global-long-period-calls-left` <= 0 || `x-plenty-global-short-period-calls-left` <= 0

    def longPeriodSecondsToWait: Int = {
      val left = `x-plenty-global-long-period-decay` - secondsPassed + 1
      if ( left > 0 ) left else 0
    }

    def shortPeriodSecondsToWait: Int = {
      val left = `x-plenty-global-short-period-decay` - secondsPassed + 1
      if ( left > 0 ) left else 0
    }

    def secondsToWait: Int = {
      ( `x-plenty-global-short-period-calls-left` <= 0 ,
        `x-plenty-global-long-period-calls-left` <= 0) match {
        case (_, true) => longPeriodSecondsToWait
        case (true, _) => shortPeriodSecondsToWait
        case _ => 0
      }
    }

    def shouldWait: Boolean =
      limitReached && ( longPeriodSecondsToWait > 0 || shortPeriodSecondsToWait > 0)
  }

  private def createLimitFromHeaders(headers: Seq[HttpHeader]): RequestLimits = {
    def getLimitValue(name: String): Int = headers.find(_.is(name.toLowerCase())) match {
      case Some(header) => header.value().toInt
      case None => 1000
    }
    RequestLimits(
      `x-plenty-global-long-period-limit` = getLimitValue("x-plenty-global-long-period-limit"),
      `x-plenty-global-long-period-decay` = getLimitValue("x-plenty-global-long-period-decay"),
      `x-plenty-global-long-period-calls-left` = getLimitValue("x-plenty-global-long-period-calls-left"),
      `x-plenty-global-short-period-limit` = getLimitValue("x-plenty-global-short-period-limit"),
      `x-plenty-global-short-period-decay` = getLimitValue("x-plenty-global-short-period-decay"),
      `x-plenty-global-short-period-calls-left` = getLimitValue("x-plenty-global-short-period-calls-left")
    )
  }
  
  private var limits: Option[Map[OperationType, RequestLimits]] = None
  private def updateLimit(operationType: OperationType, newLimits: RequestLimits): Unit =
    limits = Some(limits.getOrElse(Map.empty) + (operationType -> newLimits))

  private def getLimits(operationType: OperationType): Option[RequestLimits] =
    limits.getOrElse(Map.empty).get(operationType)

  private def call(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    // @todo: Make use of connection pool
    logger.info(s"Calling ${request.uri.toString()}")
    val operationType: OperationType = request.method match {
      case HttpMethods.POST => WriteOperation()
      case HttpMethods.PUT => WriteOperation()
      case HttpMethods.DELETE => WriteOperation()
      case _ => ReadOperation()
    }
    getLimits(operationType) match {
      case Some(limit) => if (limit.shouldWait) {
        println(s"WAITING FOR ${limit.secondsToWait} SECONDS")
        Thread.sleep(limit.secondsToWait * 1000)
        println("CONTINUE")
      }
      case None =>
    }
    Http().singleRequest(request).flatMap(_.toStrict(strictTimeout))
      .flatMap (
      response => {
        updateLimit(operationType, createLimitFromHeaders(response.headers))
        response.entity.dataBytes.runReduce(_ ++ _)
          .map { entity => println(entity.utf8String); Json.parse(entity.utf8String) }
    })
  }

  private def get(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    call(request.withMethod(HttpMethods.GET))
  }

  private def delete(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    call(request.withMethod(HttpMethods.DELETE))
  }

  private def post(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    call(request.withMethod(HttpMethods.POST))
  }

  private def put(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    call(request.withMethod(HttpMethods.PUT))
  }

  private def addUriPath(request: HttpRequest, path: String): HttpRequest = {
    request.withUri(request.uri.withPath(request.uri.path + path))
  }

  def apply(baseUri: Uri, username: String, password: String)(implicit actorSystem: ActorSystem): RestAPIClient =
    new RestAPIClient(baseUri, username, password)
}

class RestAPIClient private(baseUri: Uri, username: String, password: String)(implicit actorSystem: ActorSystem) {
  import OAuthTokenImplicits._
  implicit val baseRequest: HttpRequest = HttpRequest(uri = baseUri)
  private implicit val executionContext: ExecutionContext = actorSystem.dispatcher
  private var accessToken: Option[OAuthToken] = None

  def login(username: String, password: String): Future[JsValue] = {
    post("/login", JsObject(
      Seq(
        "username" -> JsString(username),
        "password" -> JsString(password)
      )
    ))
  }

  // @TODO: Refresh token when expired
  def obtainAuthToken(): Future[Option[OAuthToken]] = {
    accessToken match {
      case Some(token) if !token.expired() =>
        Future { accessToken }
      case _ =>
        login().map {
          response =>
            accessToken = Some(Json.fromJson[OAuthToken](response).get)
            accessToken
        }
    }
  }

  def login(): Future[JsValue] = {
    login(username, password)
  }

  def items(params: Option[Seq[(String, Any)]] = None): Future[JsValue] = {
    secureGet("/items", params)
  }

  def categories(params: Option[Seq[(String, Any)]] = None): Future[JsValue] = {
    secureGet("/categories", params)
  }

  def units(): Future[JsValue] = {
    secureGet("/items/units")
  }

  def createItems(params: JsValue): Future[JsValue] = {
    securePost("/items", params)
  }

  def getAttributeValues(attributeId: Int, currentPage: Int): Future[JsValue] = {
    secureGet(s"/items/attributes/${attributeId}/values", Some(Seq(
      "itemsPerPage" -> 1000,
      "page" -> currentPage
    )))
  }

  def deleteAttributeValue(attributeId: Int, valueId: Int): Future[JsValue] = {
    secureDelete(s"/items/attributes/${attributeId}/values/$valueId")
  }

  def postAttributeValue(attributeId: Int, value: AttributeValueRequest): Future[JsValue] = {
    println(Json.toJson(value))
    securePost(s"/items/attributes/${attributeId}/values", Json.toJson(value))
  }

  def getManufacturers(currentPage: Int = 1): Future[JsValue] = {
    secureGet("/items/manufacturers", Some(Seq(
      "itemsPerPage" -> 300,
      "page" -> currentPage
    )))
  }

  private def withAuthToken[A](future: OAuthToken => Future[A]): Future[A] = {
    for {
      token <- obtainAuthToken()
      f <- future(token.get)
    } yield f
  }

  private def secureGet(method: String, params: Option[Seq[(String, Any)]] = None): Future[JsValue] = {
    withAuthToken {
      token =>
        get(method, params)(baseRequest.withHeaders(new Authorization(OAuth2BearerToken(token.accessToken))))
    }
  }

  private def secureDelete(method: String, params: Option[Seq[(String, Any)]] = None): Future[JsValue] = {
    withAuthToken {
      token =>
        delete(method, params)(baseRequest.withHeaders(new Authorization(OAuth2BearerToken(token.accessToken))))
    }
  }

  private def securePost(method: String, params: JsValue): Future[JsValue] = {
    withAuthToken {
      token =>
        post(method, params)(baseRequest.withHeaders(new Authorization(OAuth2BearerToken(token.accessToken))))
    }
  }

  private def securePut(method: String, params: JsValue): Future[JsValue] = {
    withAuthToken {
      token =>
        put(method, params)(baseRequest.withHeaders(new Authorization(OAuth2BearerToken(token.accessToken))))
    }
  }

  private def makeParams(query: Query, params: Option[Seq[(String, Any)]]): Seq[(String, String)] = {
    (query ++ params.getOrElse(Seq.empty).filter(_._2 != None).map {
      case (key, value @Some(_)) => key -> value.toString
      case (key, value) => key -> value.toString
    })
  }

  private def delete(method: String, params: Option[Seq[(String, Any)]])(implicit requestBase: HttpRequest): Future[JsValue] = {
    val request = RestAPIClient.addUriPath(requestBase, method)
    delete(request.withUri(
      request.uri.withQuery(Uri.Query(makeParams(request.uri.query(), params): _*))
    ))
  }

  private def get(method: String, params: Option[Seq[(String, Any)]])(implicit requestBase: HttpRequest): Future[JsValue] = {
    val request = RestAPIClient.addUriPath(requestBase, method)
    get(request.withUri(
      request.uri.withQuery(Uri.Query(makeParams(request.uri.query(), params): _*))
    ))
  }

  private def get(implicit request: HttpRequest): Future[JsValue] = {
    RestAPIClient.get(request)
  }

  private def delete(implicit request: HttpRequest): Future[JsValue] = {
    RestAPIClient.delete(request)
  }

  private def post(method: String, json: JsValue)(implicit requestBase: HttpRequest): Future[JsValue] = {
    val request = RestAPIClient.addUriPath(requestBase, method)
      .withEntity(ContentTypes.`application/json`, json.toString())
    post(request)
  }

  private def put(method: String, json: JsValue)(implicit requestBase: HttpRequest): Future[JsValue] = {
    val request = RestAPIClient.addUriPath(requestBase, method)
      .withEntity(ContentTypes.`application/json`, json.toString())
    put(request)
  }

  private def post(request: HttpRequest): Future[JsValue] = {
    RestAPIClient.post(request)
  }

  private def put(request: HttpRequest): Future[JsValue] = {
    RestAPIClient.put(request)
  }


  /**
   * METHODS TO WORK WITH CONNECTOR
   */
  def variations(params: (String, Any)*): Future[VariationsPage] = {
    secureGet("/items/variations", Some(Seq(params: _*)))
      .map(response => {
        response.validate[VariationsPage] match {
          case JsSuccess(value, _) => value
          case JsError(errors) => println(errors); throw new Exception(errors.toString())
        }
      })
  }

  def createItems(request: Array[Item]): Future[JsValue] = {
    securePost("/items", Json.toJson(request))
  }

  def createVariation(itemId: Int, variation: ItemVariation): Future[JsValue] = {
    securePost(s"/items/${itemId}/variations", Json.toJson(variation))
  }

  def stocks(itemId: Int, variationId: Int): Future[JsValue] = {
    secureGet(s"/items/$itemId/variations/$variationId/stock")
  }

  def bookStocks(itemId: Int, variationId: Int, request: BookIncomingStockRequest): Future[JsValue] = {
    securePut(s"/items/$itemId/variations/$variationId/stock/bookIncomingItems", Json.toJson(request))
  }

  def stockCorrection(itemId: Int, variationId: Int, request: StockCorrectionRequest): Future[JsValue] = {
    securePut(s"/items/$itemId/variations/$variationId/stock/correction", Json.toJson(request))
  }
}