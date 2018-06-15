package org.materialsdatafacility.connect.models

import play.api.http.Writeable
import play.api.libs.json._
import play.api.mvc.Codec

case class ConvertRequest(dataCite: DataCite, data: Seq[String], test: Option[Boolean])

object ConvertRequest {
  implicit val convertRequestWrites = new Writes[ConvertRequest] {
    def writes(request: ConvertRequest): JsObject = {
      Json.obj(
        "dc" -> request.dataCite,
        "data"->request.data,
        "test"-> JsBoolean(request.test.getOrElse(true))
      )
    }
  }
}

