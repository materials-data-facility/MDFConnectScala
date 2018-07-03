package org.materialsdatafacility.connect.services

import java.net.URL

import com.ning.http.client.Realm.AuthScheme
import org.materialsdatafacility.connect.models.{AccessToken, ConvertRequest}
import play.api.http.{Status, Writeable, _}
import play.api.libs.ws.WS
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Bindings for the MDF Connect REST API. This service is part of the Materials Data Facility project.
  *
  * @constructor Create a new connection to the service by passing in a Play WS webservice client, as
  *              well as the client ID, and secret for the Globus account to be used with MDF Connect. Also
  *              accepts the MDF Connect URL to use to be able to switch between production and test
  * @param ws            Instance of the play webservice library
  * @param clientID      Globus Client ID
  * @param secret        Associated secret
  * @param mdfConnectURL The URL to use to call MDF Connect operations
  */
class MDFPublishService(ws: WS.type, clientID: String, secret: String, mdfConnectURL: URL) {

  private val globusAuthURL = "https://auth.globus.org/v2/oauth2/token"

  /**
    * List of scopes we will need in order to interact with MDF Connect
    */
  private val scopes = "https://auth.globus.org/scopes/ab24b500-37a2-4bad-ab66-d8232c18e6e5/publish_api " +
    "urn:globus:auth:scope:data.materialsdatafacility.org:all " +
    "https://auth.globus.org/scopes/ab24b500-37a2-4bad-ab66-d8232c18e6e5/publish_api " +
    "https://auth.globus.org/scopes/c17f27bb-f200-486a-b785-2a25e82af505/connect"


  // Some implicit methods to allow passing of json objects around
  implicit def writeable(implicit codec: Codec): Writeable[ConvertRequest] = {
    // assuming RepositoryMetadata has a .toString
    Writeable(data => codec.encode(data.toString))
  }

  implicit def contentType(implicit codec: Codec): ContentTypeOf[ConvertRequest] = {
    // for text/plain
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  /**
    * Authenticate with Globus Auth to obtain a valid access token. This uses the client ID and secret
    * associated with this instances
    *
    * @return Generated Access Token
    */
  def authenticate(): Future[Try[AccessToken]] = {
    ws.url(globusAuthURL)
      .withAuth(clientID, secret, AuthScheme.BASIC)
      .post(Map(
        "grant_type" -> Seq("client_credentials"),
        "scope" -> Seq(scopes))) map { result =>
      result.status match {
        case Status.OK => {
          // Find the MDF Dataset Submission scope and keep ahold of its token
          val tokens = (result.json \ "other_tokens").as[Seq[AccessToken]]
          tokens.find((p: AccessToken) => p.resourceServer.equals("mdf_dataset_submission")) match {
            case Some(accessToken) => Success(accessToken)
            case None => Failure(new Exception("Token for MDF Connect not found"))
          }
        }
        case Status.FORBIDDEN => Failure(new Exception("Can't authenticated client"))
        case _ => Failure(new Exception(result.body))
      }
    }
  }

  /**
    * Submit a request to the MDF Connect service to start up a file injestion process
    *
    * @param convertRequest Completed ConvertRequest object
    * @param accessToken    Access token obtained by calling authenticate()
    * @return Result string or Failure
    */
  def submitConvertRequest(convertRequest: ConvertRequest, accessToken: AccessToken): Future[Try[String]] = {

    /**
      * @todo - this should work with an implicit writer instead of explicitly calling
      *       the writes method.
      */
    val body = ConvertRequest.convertRequestWrites.writes(convertRequest)
    ws.url(mdfConnectURL.toExternalForm)
      .withHeaders("Authorization" -> accessToken.accessToken)
      .post(body) map { result =>
      result.status match {
        case Status.ACCEPTED => {
          Success(result.json.toString())
        }
        case _ => {
          Failure(new Exception("Convert request failed " + result.status + "-->" + result.body))
        }
      }
    }
  }
}

