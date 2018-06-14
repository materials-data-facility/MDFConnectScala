package org.materialsdatafacility.connect.services

import com.ning.http.client.Realm.AuthScheme
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.http.{ContentTypeOf, Writeable}
import play.api.libs.ws.{Response, WS}
import play.api.libs.ws.WS.WSRequestHolder
import play.api.http.Status

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class MDFPublishServiceSpec extends Specification with Mockito {
  val clientId = "123-456-7890"
  val secret = "shh"

  "Given an MDF Publish Service" >> {
    "When I attempt to authenticate with a valid client and secret" >> {
      "then I should receive a success response" >> {
        val ws = generateAuthMocks(Status.OK)

        val mdfConnect = new MDFPublishService(ws, clientId, secret)
        val resultFuture = mdfConnect.authenticate()

        val result = Await.result(resultFuture, 10 seconds)
        result must beASuccessfulTry[String]
      }
    }

    "When I attempt to authenticate with a invalid  secret" >> {
      "then I should receive a failure response" >> {
        val ws = generateAuthMocks(Status.FORBIDDEN)

        val mdfConnect = new MDFPublishService(ws, clientId, secret)
        val resultFuture = mdfConnect.authenticate()

        val result = Await.result(resultFuture, 10 seconds)
        result must beAFailedTry[String]
      }
    }
  }

  "Given an MDF Publish service with an invalid URL" >> {
    "When I attempt to authenticate" >> {
      "Then it should fail" >> {
        val ws = mock[WS.type]
        val wsWithURL = mock[WSRequestHolder]
        val wsWithAuth = mock[WSRequestHolder]

        {
          wsWithURL.withAuth(clientId, secret, AuthScheme.BASIC).returns(wsWithAuth)
          ws.url("https://BADURL").returns(wsWithURL)
          wsWithAuth.post(anyMap)(anyObject[Writeable[Map[_, Any]]], anyObject[ContentTypeOf[Map[_, Any]]]).returns(Future.failed(new Exception("bad url")))
          val mdfConnect = new MDFPublishService(ws, clientId, secret)
          val resultFuture = mdfConnect.authenticate()
        } must throwA[Exception]
      }
    }
  }

  def generateAuthMocks(resultStatus: Int): WS.type = {
    val ws = mock[WS.type]
    val wsWithURL = mock[WSRequestHolder]
    val wsWithAuth = mock[WSRequestHolder]
    val response = mock[Response]


    wsWithURL.withAuth(clientId, secret, AuthScheme.BASIC).returns(wsWithAuth)
    ws.url("https://auth.globus.org/v2/oauth2/token").returns(wsWithURL)
    wsWithAuth.post(anyMap)(anyObject[Writeable[Map[_, Any]]], anyObject[ContentTypeOf[Map[_, Any]]]).returns(Future(response))
    response.status.returns(resultStatus)
    ws
  }

}
