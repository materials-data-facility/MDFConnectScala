package org.materialsdatafacility.connect.models

import play.api.libs.json._

/**
  * Case class to represent a single request to injest data by MDF Connect
  * @param dataCite Data citation for the dataset
  * @param data List of URLs where MDF Connect can download data
  * @param test Is this a test run, or the real thing?
  */
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

