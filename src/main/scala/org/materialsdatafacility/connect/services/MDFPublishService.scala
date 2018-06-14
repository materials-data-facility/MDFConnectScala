package org.materialsdatafacility.connect.services

import com.ning.http.client.Realm.AuthScheme
import play.api.http.Status
import play.api.libs.json.{JsArray, JsObject, JsString}
import play.api.libs.ws.WS

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class MDFPublishService(ws: WS.type, clientID:String, secret:String) {

  def authenticate(): Future[Try[String]] = {
    ws.url("https://auth.globus.org/v2/oauth2/token")
      .withAuth(clientID, secret, AuthScheme.BASIC)
      .post(Map(
        "grant_type" -> Seq("client_credentials"),
        "scope" -> Seq("https://auth.globus.org/scopes/ab24b500-37a2-4bad-ab66-d8232c18e6e5/publish_api " +
          "urn:globus:auth:scope:data.materialsdatafacility.org:all " +
          "https://auth.globus.org/scopes/ab24b500-37a2-4bad-ab66-d8232c18e6e5/publish_api " +
          "https://auth.globus.org/scopes/c17f27bb-f200-486a-b785-2a25e82af505/connect"))) map { result =>
      result.status match{
        case Status.OK => Success(s"Client ${clientID} Authenticated")
        case Status.FORBIDDEN => Failure(new Exception("Can't authenticated client"))
        case _ => Failure(new Exception(result.body))
      }
    }
  }

  def create_dc(title: String, authors:Seq[String]):JsObject = {
    JsObject(Seq(
      "title" -> JsString(title),
      "authors" -> JsArray(authors map{a=>JsString(a)})
    ))
  }

}

