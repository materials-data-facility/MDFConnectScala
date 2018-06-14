package org.materialsdatafacility.connect.models

case class ConvertRequest(dataCite: DataCite, data: Seq[String], test: Option[Boolean])

