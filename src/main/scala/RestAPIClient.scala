package org.hardsoft321.plentymarkets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model._
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

object RestAPIClient extends StrictLogging {
  private val strictTimeout = 1.seconds

  private def call(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    // @todo: Make use of connection pool
    logger.info(s"Calling ${request.uri.toString()}")
    Http().singleRequest(request).flatMap {
      // @todo: Catch Exceptions
      case response @HttpResponse(StatusCodes.OK, _, _, _) =>
        response.entity.dataBytes
          .runReduce(_ ++ _)
          .map { entity =>
            Json.parse(entity.utf8String) match {
              case responseBody: JsValue => responseBody
            }
          }

    }
  }

  private def get(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
    call(request.withMethod(HttpMethods.GET))
  }

  private def post(request: HttpRequest)(implicit actorSystem: ActorSystem): Future[JsValue] = {
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