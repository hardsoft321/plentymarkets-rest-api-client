package org.hardsoft321.plentymarkets

import ItemRequests.{AttributeValue => AttributeValueRequest}
import ItemResponses.{Response, ValidationErrorResponse, AttributeValue => AttributeValueResponse}

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
          .map { entity => Json.parse(entity.utf8String) }
    })
  }

  private def get(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    call(request.withMethod(HttpMethods.GET))
  }

  private def post(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    call(request.withMethod(HttpMethods.POST))
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
      "itemsPerPage" -> 300,
      "page" -> currentPage
    )))
  }

  def postAttributeValue(attributeId: Int, value: AttributeValueRequest): Future[JsValue] = {
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

  private def securePost(method: String, params: JsValue): Future[JsValue] = {
    withAuthToken {
      token =>
        post(method, params)(baseRequest.withHeaders(new Authorization(OAuth2BearerToken(token.accessToken))))
    }
  }

  private def makeParams(query: Query, params: Option[Seq[(String, Any)]]): Map[String, String] = {
    (query ++ params.getOrElse(Seq.empty).filter(_._2 != None).map {
      case (key, value @Some(_)) => key -> value.toString
      case (key, value) => key -> value.toString
    }).toMap
  }

  private def get(method: String, params: Option[Seq[(String, Any)]])(implicit requestBase: HttpRequest): Future[JsValue] = {
    val request = RestAPIClient.addUriPath(requestBase, method)
    get(request.withUri(
      request.uri.withQuery(Uri.Query(makeParams(request.uri.query(), params)))
    ))
  }

  private def get(implicit request: HttpRequest): Future[JsValue] = {
    RestAPIClient.get(request)
  }

  private def post(method: String, json: JsValue)(implicit requestBase: HttpRequest): Future[JsValue] = {
    val request = RestAPIClient.addUriPath(requestBase, method)
      .withEntity(ContentTypes.`application/json`, json.toString())
    post(request)
  }

  private def post(request: HttpRequest): Future[JsValue] = {
    RestAPIClient.post(request)
  }
}