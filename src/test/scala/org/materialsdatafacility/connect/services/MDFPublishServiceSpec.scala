package org.materialsdatafacility.connect.services

import java.net.URL

import com.ning.http.client.Realm.AuthScheme
import org.materialsdatafacility.connect.models.{AccessToken, ConvertRequest, DataCite}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.http.{ContentTypeOf, Writeable}
import play.api.libs.ws.{Response, WS}
import play.api.libs.ws.WS.WSRequestHolder
import play.api.http.Status
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.specs2.matcher.Matchers

class MDFPublishServiceSpec extends Specification with Mockito {
  val clientId = "123-456-7890"
  val secret = "shh"

  val validJsonResult = Json.parse(
    """
      |{
      |  "access_token" : "Agk1o4vY6e43PwMdd2bB5ep1V8DWVWwkwedGoPga13pqmwj2eeUOCpMe67O938pJ0336MBJ8NPjwm7ujaMEMGIX5ad",
      |  "expires_in" : 172800,
      |  "resource_server" : "publish.api.globus.org",
      |  "token_type" : "Bearer",
      |  "other_tokens" : [ {
      |    "access_token" : "AgMg5dvMbdmx0kKYVwbq19bYMjWYkjBv2g6ap31XMzWygOokgEHOCmxdrPmlVXgEKBX63GN98D4YXkFvwPDPYFrXgm",
      |    "scope" : "urn:globus:auth:scope:data.materialsdatafacility.org:all",
      |    "expires_in" : 172800,
      |    "resource_server" : "data.materialsdatafacility.org",
      |    "token_type" : "Bearer"
      |  }, {
      |    "access_token" : "AgmlPWw6PyWl7o34qYnNjbzXw5eW3NP0arp55b07ok7x5O86QJh8CBEdwJBqOXMlJjXWX1azw1Ea3mu8zbQbGtMByM",
      |    "scope" : "https://auth.globus.org/scopes/c17f27bb-f200-486a-b785-2a25e82af505/connect",
      |    "expires_in" : 172800,
      |    "resource_server" : "mdf_dataset_submission",
      |    "token_type" : "Bearer"
      |  } ],
      |  "scope" : "https://auth.globus.org/scopes/ab24b500-37a2-4bad-ab66-d8232c18e6e5/publish_api"
      |}
    """.stripMargin)

  "Given an MDF Publish Service" >> {
    "When I attempt to authenticate with a valid client and secret" >> {
      "then I should receive a success response" >> {
        val ws = generateAuthMocks(Status.OK, validJsonResult)

        val mdfConnect = new MDFPublishService(ws, clientId, secret, new URL("http://www.mdf.com"))
        val resultFuture = mdfConnect.authenticate()

        val result = Await.result(resultFuture, 10 seconds)
        result must beASuccessfulTry[AccessToken]
      }
    }

    "Given an MDF Publish Service" >> {
      "When I attempt to authenticate with a valid client and secret but no valid access token is available" >> {
        "then I should receive a success response" >> {
          val invalidJsonResult = Json.parse(
            """
              |{
              |  "access_token" : "Agk1o4vY6e43PwMdd2bB5ep1V8DWVWwkwedGoPga13pqmwj2eeUOCpMe67O938pJ0336MBJ8NPjwm7ujaMEMGIX5ad",
              |  "expires_in" : 172800,
              |  "resource_server" : "publish.api.globus.org",
              |  "token_type" : "Bearer",
              |  "other_tokens" : [ {
              |    "access_token" : "AgMg5dvMbdmx0kKYVwbq19bYMjWYkjBv2g6ap31XMzWygOokgEHOCmxdrPmlVXgEKBX63GN98D4YXkFvwPDPYFrXgm",
              |    "scope" : "urn:globus:auth:scope:data.materialsdatafacility.org:all",
              |    "expires_in" : 172800,
              |    "resource_server" : "data.materialsdatafacility.org",
              |    "token_type" : "Bearer"
              |  } ],
              |  "scope" : "https://auth.globus.org/scopes/ab24b500-37a2-4bad-ab66-d8232c18e6e5/publish_api"
              |}
            """.stripMargin)
          val ws = generateAuthMocks(Status.OK, invalidJsonResult)

          val mdfConnect = new MDFPublishService(ws, clientId, secret, new URL("http://www.mdf.com"))
          val resultFuture = mdfConnect.authenticate()

          val result = Await.result(resultFuture, 10 seconds)
          result must beAFailedTry[AccessToken]
        }
      }
    }

      "When I attempt to authenticate with a invalid  secret" >> {
      "then I should receive a failure response" >> {
        val ws = generateAuthMocks(Status.FORBIDDEN, validJsonResult)

        val mdfConnect = new MDFPublishService(ws, clientId, secret, new URL("http://www.mdf.com"))
        val resultFuture = mdfConnect.authenticate()

        val result = Await.result(resultFuture, 10 seconds)
        result must beAFailedTry[AccessToken]
      }
    }

    "And there is an MDF Connect service is down" >> {
      "When I attempt to authenticate with a invalid  secret" >> {
        "then I should receive a failure response" >> {
          val ws = generateAuthMocks(Status.GONE, validJsonResult)

          val mdfConnect = new MDFPublishService(ws, clientId, secret, new URL("http://www.mdf.com"))
          val resultFuture = mdfConnect.authenticate()

          val result = Await.result(resultFuture, 10 seconds)
          result must beAFailedTry[AccessToken]
        }
      }
    }
  }

  "Given an MDF Publish service with an invalid Globus Auth URL" >> {
    "When I attempt to authenticate" >> {
      "Then it should fail" >> {
        val ws = mock[WS.type]
        val wsWithURL = mock[WSRequestHolder]
        val wsWithAuth = mock[WSRequestHolder]

        {
          wsWithURL.withAuth(clientId, secret, AuthScheme.BASIC).returns(wsWithAuth)
          ws.url("https://BADURL").returns(wsWithURL)
          wsWithAuth.post(anyMap)(anyObject[Writeable[Map[_, Any]]], anyObject[ContentTypeOf[Map[_, Any]]]).returns(Future.failed(new Exception("bad url")))
          val mdfConnect = new MDFPublishService(ws, clientId, secret, new URL("http://www.mdf.com"))
          val resultFuture = mdfConnect.authenticate()
        } must throwA[Exception]
      }
    }
  }

  "Given an MDF Publish Service">>{
    val ws = mock[WS.type]
    val mdfConnect = new MDFPublishService(ws, clientId, secret, new URL("http://www.mdf.com"))
    "when I submit a convert request">> {
      val dc = new DataCite(Seq("My title"), Seq("Bob Dobbo"), None, None, Some(1984), None, None, None, None)
      val convertRequest = new ConvertRequest(dc, Seq("http://foo.com/bar", "http://bar.com/foo"), Some(false))
      val accessToken = new AccessToken("aTOKEN", "scopescopescope", 42, "resource", "bearer")

      val wsWithURL = mock[WSRequestHolder]
      val wsWithHeaders = mock[WSRequestHolder]
      val response = mock[Response]


      ws.url("http://www.mdf.com").returns(wsWithURL)
      wsWithURL.withHeaders("Authorization" -> "aTOKEN").returns(wsWithHeaders)
      wsWithHeaders.post(anyObject[JsObject])(anyObject[Writeable[JsObject]], anyObject[ContentTypeOf[JsObject]]).returns(Future(response))
      response.status.returns(Status.ACCEPTED)
      response.json.returns(JsObject(Seq("ok" -> JsString("ok"))))
      val resultFuture = mdfConnect.submitConvertRequest(convertRequest, accessToken)
      "then I should receive a success response" >> {
        val result = Await.result(resultFuture, 10 seconds)
        result must beSuccessfulTry[String]
      }
    }
  }

  "Given an MDF Publish Service">>{
    val ws = mock[WS.type]
    val mdfConnect = new MDFPublishService(ws, clientId, secret, new URL("http://www.mdf.com"))
    "when I submit an invalid convert request">> {
      val dc = new DataCite(Seq(""), Seq(""), None, None, Some(1984), None, None, None, None)
      val convertRequest = new ConvertRequest(dc, Seq("http://foo.com/bar", "http://bar.com/foo"), Some(false))
      val accessToken = new AccessToken("aTOKEN", "scopescopescope", 42, "resource", "bearer")

      val wsWithURL = mock[WSRequestHolder]
      val wsWithHeaders = mock[WSRequestHolder]
      val response = mock[Response]


      ws.url("http://www.mdf.com").returns(wsWithURL)
      wsWithURL.withHeaders("Authorization" -> "aTOKEN").returns(wsWithHeaders)
      wsWithHeaders.post(anyObject[JsObject])(anyObject[Writeable[JsObject]], anyObject[ContentTypeOf[JsObject]]).returns(Future(response))
      response.status.returns(Status.BAD_REQUEST)
      val resultFuture = mdfConnect.submitConvertRequest(convertRequest, accessToken)
      "then I should receive a success response" >> {
        val result = Await.result(resultFuture, 10 seconds)
        result must beFailedTry[String]
      }
    }
  }


  def generateAuthMocks(resultStatus: Int, resultJson: JsValue): WS.type = {
    val ws = mock[WS.type]
    val wsWithURL = mock[WSRequestHolder]
    val wsWithAuth = mock[WSRequestHolder]
    val response = mock[Response]


    wsWithURL.withAuth(clientId, secret, AuthScheme.BASIC).returns(wsWithAuth)
    ws.url("https://auth.globus.org/v2/oauth2/token").returns(wsWithURL)
    wsWithAuth.post(anyMap)(anyObject[Writeable[Map[_, Any]]], anyObject[ContentTypeOf[Map[_, Any]]]).returns(Future(response))
    response.status.returns(resultStatus)
    response.json returns resultJson
    ws
  }
}
