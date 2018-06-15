package org.materialsdatafacility.connect.services

import com.ning.http.client.Realm.AuthScheme
import org.materialsdatafacility.connect.models.{AccessToken, ConvertRequest}
import play.api.http.{Status, Writeable}
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.http._
import play.api.mvc._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class MDFPublishService(ws: WS.type, clientID:String, secret:String) {

  implicit def writeable(implicit codec: Codec): Writeable[ConvertRequest] = {
    // assuming RepositoryMetadata has a .toString
    Writeable(data => codec.encode(data.toString))
  }

  implicit def contentType(implicit codec: Codec): ContentTypeOf[ConvertRequest] = {
    // for text/plain
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  def authenticate(): Future[Try[AccessToken]] = {
    ws.url("https://auth.globus.org/v2/oauth2/token")
      .withAuth(clientID, secret, AuthScheme.BASIC)
      .post(Map(
        "grant_type" -> Seq("client_credentials"),
        "scope" -> Seq("https://auth.globus.org/scopes/ab24b500-37a2-4bad-ab66-d8232c18e6e5/publish_api " +
          "urn:globus:auth:scope:data.materialsdatafacility.org:all " +
          "https://auth.globus.org/scopes/ab24b500-37a2-4bad-ab66-d8232c18e6e5/publish_api " +
          "https://auth.globus.org/scopes/c17f27bb-f200-486a-b785-2a25e82af505/connect"))) map { result =>
      result.status match{
        case Status.OK => {
          val tokens = (result.json\"other_tokens").as[Seq[AccessToken]]
          tokens.find((p:AccessToken) => p.resourceServer.equals("mdf_dataset_submission")) match{
            case Some(accessToken) => Success(accessToken)
            case None => Failure(new Exception("Token for MDF Connect not found"))
          }
        }
        case Status.FORBIDDEN => Failure(new Exception("Can't authenticated client"))
        case _ => Failure(new Exception(result.body))
      }
    }
  }

  def submitConvertRequest(convertRequest: ConvertRequest, accessToken: AccessToken): Future[Try[String]] = {

    ws.url("https://34.193.81.207:5000/convert")
      .withHeaders("Authorization"->accessToken.accessToken)
      .post(convertRequest) map { result =>
      result.status match{
        case Status.OK => {
          println(result.json)
          Success(result.json.toString())
        }
        case _ =>{
          Failure(new Exception("Convert request failed "+result.status+"-->"+result.body))
        }
      }
      }
  }
}

