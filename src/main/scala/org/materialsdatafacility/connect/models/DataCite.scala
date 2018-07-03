package org.materialsdatafacility.connect.models

import org.joda.time.DateTime
import play.api.libs.json._

/**
  * A complete citation for a data submission
  * @param title List of titles
  * @param authors List of authors
  * @param affiliations Affiliations for the authors - not currently used -
  * @param publisher Name of the data publisher. Defaults to Materials Data Facility
  * @param publication_year Defaults to the current year
  * @param resource_type
  * @param description Full text description
  * @param dataset_doi DOI assigned to this dataset
  * @param related_dois List of related DOIs
  */
case class DataCite(title: Seq[String], authors: Seq[String],
                    affiliations: Option[Seq[String]],
                    publisher: Option[String],
                    publication_year: Option[Int],
                    resource_type: Option[String],
                    description: Option[String],
                    dataset_doi: Option[String],
                    related_dois: Option[Seq[String]])

object DataCite {
  implicit val dataCiteWrites = new Writes[DataCite] {
    def writes(dc: DataCite): JsObject = {

      val creators = dc.authors map { creator =>
        val (familyName, given) = extractNameParts(creator)
        val creatorName = if (familyName.isEmpty) given else (s"$familyName, $given")
        Json.obj(
          "creatorName" -> creatorName,
          "familyName" -> familyName,
          "givenName" -> given
        )
      }

      val titles = dc.title map { title =>
        Json.obj("title" -> title)
      }

      val (familyName, given) = extractNameParts(dc.authors.head)

      val defaultPublicationYear = new DateTime().year().get()

      // Basic object with the required fields
      val basicObj = Json.obj(
        "titles" -> JsArray(titles),
        "creators" -> JsArray(creators),
        "publisher" -> JsString(dc.publisher.getOrElse("Materials Data Facility")),
        "publicationYear" -> JsString(dc.publication_year.getOrElse(defaultPublicationYear).toString)
      )

      // Extend with optional properties
      val objWithDescription = if (dc.description.isDefined) basicObj ++ Json.obj("descriptions" -> JsArray(Seq(Json.obj(
        "description" -> JsString(dc.description.get),
        "descriptionType" -> JsString("Other")
      )))) else basicObj

      val objWithResourceType = if (dc.resource_type.isDefined) objWithDescription ++ Json.obj(
        "resourceType" -> Json.obj("resourceType" -> dc.resource_type.get,
          "resourceTypeGeneral" -> JsString("Dataset"))) else objWithDescription

      val objWithDOI = if (dc.dataset_doi.isDefined) objWithResourceType ++ Json.obj(
        "identifier" -> Json.obj(
          "identifier" -> dc.dataset_doi.get,
          "identifierType" -> "DOI")) else objWithResourceType

      val relatedDOIEntry = if (dc.related_dois.isDefined) dc.related_dois.get map { doi =>
        Json.obj(
          "relatedIdentifier" -> doi,
          "relatedIdentifierType" -> "DOI",
          "relationType" -> "IsPartOf"
        )
      } else List()

      if (dc.related_dois.isDefined) objWithDOI ++ Json.obj(
        "relatedIdentifiers" -> JsArray(relatedDOIEntry)) else objWithDOI
    }
  }

  private def extractNameParts(nameStr: String): (String, String) = {
    if (nameStr.contains(",")) nameStr.split(",").toList match {
      case last :: first :: Nil => (last.trim, first.trim)
      case _ => ("", "")
    }
    else if (nameStr.contains(";")) nameStr.split(";").toList match {
      case last :: first :: Nil => (last.trim, first.trim)
      case _ => ("", "")
    }
    else if (nameStr.contains(" ")) nameStr.split(" ").toList match {
      case first :: last :: Nil => (last.trim, first.trim)
      case _ => ("", "")
    }
    else ("", nameStr)
  }
}

