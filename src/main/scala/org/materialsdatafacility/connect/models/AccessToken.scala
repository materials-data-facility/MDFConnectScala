package org.materialsdatafacility.connect.models

import play.api.libs.json._

/**
  * Token go access a specific scope obtained from Globus Auth
  * @param accessToken Token to be used when submitting operations to MDF Connect
  * @param scope
  * @param expiresIn
  * @param resourceServer
  * @param tokenType
  */
case class AccessToken (accessToken: String, scope:String, expiresIn:Int, resourceServer:String, tokenType:String)

object AccessToken{
  implicit val accessTokenReads = new Reads[AccessToken] {
    def reads(json: JsValue): JsResult[AccessToken] =
      JsSuccess(AccessToken(
        (json \ "access_token").as[String],
        (json \ "scope").as[String],
        (json \ "expires_in").asInstanceOf[JsNumber].value.toInt,
        (json \ "resource_server").as[String],
        (json \ "token_type").as[String]))
  }


  }
