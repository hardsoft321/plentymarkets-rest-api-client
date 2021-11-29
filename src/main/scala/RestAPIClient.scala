package org.hardsoft321.plentymarkets

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, Uri}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}


class RestAPIClient private(baseUri: Uri, username: String, password: String)(implicit actorSystem: ActorSystem) {
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
    val request = PlentyMarketsAPIClient.addUriPath(requestBase, method)
    get(request.withUri(
      request.uri.withQuery(Uri.Query(makeParams(request.uri.query(), params)))
    ))
  }

  private def get(implicit request: HttpRequest): Future[JsValue] = {
    PlentyMarketsAPIClient.get(request)
  }

  private def post(method: String, json: JsValue)(implicit requestBase: HttpRequest): Future[JsValue] = {
    val request = PlentyMarketsAPIClient.addUriPath(requestBase, method)
      .withEntity(ContentTypes.`application/json`, json.toString())
    post(request)
  }

  private def post(request: HttpRequest): Future[JsValue] = {
    PlentyMarketsAPIClient.post(request)
  }
}