package org.materialsdatafacility.connect.models

import org.joda.time.DateTime
import play.api.libs.json._

case class DataCite(title: Seq[String], authors: Seq[String],
                    affiliations: Option[Seq[String]],
                    publisher: Option[String],
                    publication_year: Option[Int],
                    resource_type: Option[String],
                    description: Option[String],
                    dataset_doi: Option[String],
                    related_dois: Option[Seq[String]])

  object DataCite {
    implicit val dataCiteWrites = new Writes[DataCite]{
      def writes(dc: DataCite):JsObject = {

        val creators = dc.authors map{ creator =>
          val (familyName, given) = extractNameParts(creator)
          val creatorName = if (familyName.isEmpty) given else (s"$familyName, $given")
          Json.obj(
            "creatorName" -> creatorName,
            "familyName" -> familyName,
            "givenName" -> given
          )
        }

        val (familyName, given) = extractNameParts(dc.authors.head)

        val defaultPublicationYear = new DateTime().year().get()
        Json.obj(
          "titles" -> dc.title,
          "creators" -> JsArray(creators),
          "publisher" -> JsString(dc.publisher.getOrElse("Materials Data Facility")),
          "publicationYear" -> JsString(dc.publication_year.getOrElse(defaultPublicationYear).toString)
        )
      }
    }

    private def extractNameParts(nameStr: String): (String, String) = {
      if(nameStr.contains(",")) nameStr.split(",").toList match{ case last::first::Nil => (last.trim, first.trim) case _ => ("","")}
      else if(nameStr.contains(";")) nameStr.split(";").toList match{ case last::first::Nil => (last.trim, first.trim) case _ => ("","")}
      else if(nameStr.contains(" ")) nameStr.split(" ").toList match{ case first::last::Nil => (last.trim, first.trim) case _ => ("","")}
      else ("", nameStr)
    }
  }

