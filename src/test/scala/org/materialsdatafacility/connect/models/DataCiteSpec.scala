package org.materialsdatafacility.connect.models

import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.libs.json._

class DataCiteSpec extends Specification{
  "Given a DataCite with just a title and an author" >> {
    "When I generate json" >> {
      "Then it should match the schema" >> {
        val dc = new DataCite(Seq("My title", "My second title"), Seq("Bob Dobbo", "Barny Rubble"), None, None, None, None, None, None, None)

        val foo = DataCite.dataCiteWrites.writes(dc)
        val bar = Json.prettyPrint(foo)
        val currentYear = new DateTime().year().get

        bar must be equalTo
          s"""{
            |  "titles" : [ {
            |    "title" : "My title"
            |  }, {
            |    "title" : "My second title"
            |  } ],
            |  "creators" : [ {
            |    "creatorName" : "Dobbo, Bob",
            |    "familyName" : "Dobbo",
            |    "givenName" : "Bob"
            |  }, {
            |    "creatorName" : "Rubble, Barny",
            |    "familyName" : "Rubble",
            |    "givenName" : "Barny"
            |  } ],
            |  "publisher" : "Materials Data Facility",
            |  "publicationYear" : "$currentYear"
            |}""".stripMargin
      }
    }
  }

  "Given an author formatted as 'last, first">>{
    val author = "Dobbo, Bob"
    "When I generate JSON" >> {
      val json = generateJsonForCreator(author)
      "Then I should see creator name, family name, and given name extracted correctly">>{
        (json\"creatorName").as[String] must be equalTo("Dobbo, Bob")
        (json\"givenName").as[String] must be equalTo("Bob")
        (json\"familyName").as[String] must be equalTo("Dobbo")
      }
    }
  }

  "Given an author formatted as 'last; first">>{
    val author = "Dobbo; Bob"
    "When I generate JSON" >> {
      val json = generateJsonForCreator(author)
      "Then I should see creator name, family name, and given name extracted correctly">>{
        (json\"creatorName").as[String] must be equalTo("Dobbo, Bob")
        (json\"givenName").as[String] must be equalTo("Bob")
        (json\"familyName").as[String] must be equalTo("Dobbo")
      }
    }
  }

  "Given an author formatted as a single name">>{
    val author = "Prince"
    "When I generate JSON" >> {
      val json = generateJsonForCreator(author)
      "Then I should see creator name, family name, and given name extracted correctly">>{
        (json\"creatorName").as[String] must be equalTo("Prince")
        (json\"givenName").as[String] must be equalTo("Prince")
        (json\"familyName").as[String] must be equalTo("")
      }
    }
  }


  "Given an author formatted as 'first last">>{
    val author = "Bob Dobbo"
    "When I generate JSON" >> {
      val json = generateJsonForCreator(author)
      "Then I should see creator name, family name, and given name extracted correctly">>{
        (json\"creatorName").as[String] must be equalTo("Dobbo, Bob")
        (json\"givenName").as[String] must be equalTo("Bob")
        (json\"familyName").as[String] must be equalTo("Dobbo")
      }
    }
  }


  "Given the publisher is provided">>{
    val publisher = "Hearst Publishing"
    "When I generate JSON" >> {
      val json = DataCite.dataCiteWrites.writes(new DataCite(Seq("My title"),
        Seq("Me too"), None, Some(publisher), None, None, None, None, None))
      "Then I should my publisher extracted correctly">>{
        (json\"publisher").as[String] must be equalTo(publisher)
      }
    }
  }

  "Given the publication year is provided">>{
    "When I generate JSON" >> {
      val json = DataCite.dataCiteWrites.writes(new DataCite(Seq("My title"),
        Seq("Me too"), None, None, Some(1776), None, None, None, None))
      "Then I should my publisher extracted correctly">>{
        (json\"publicationYear").as[String] must be equalTo("1776")
      }
    }
  }

  "Given the desscription is provided">> {
    "When I generate JSON" >> {
      val json = DataCite.dataCiteWrites.writes(new DataCite(Seq("My title"),
        Seq("Me too"), None, None, Some(1776), None, Some("My description"), None, None))
      "Then I should see the description property encoded" >> {
        Json.prettyPrint(json) must be equalTo
          """{
            |  "titles" : [ {
            |    "title" : "My title"
            |  } ],
            |  "creators" : [ {
            |    "creatorName" : "too, Me",
            |    "familyName" : "too",
            |    "givenName" : "Me"
            |  } ],
            |  "publisher" : "Materials Data Facility",
            |  "publicationYear" : "1776",
            |  "descriptions" : [ {
            |    "description" : "My description",
            |    "descriptionType" : "Other"
            |  } ]
            |}""".stripMargin
      }
    }
  }

  "Given the resource type is provided">> {
    "When I generate JSON" >> {
      val json = DataCite.dataCiteWrites.writes(new DataCite(Seq("My title"),
        Seq("Me too"), None, None, Some(1776), Some("Resource type"), None, None, None))
      "Then I should see the description property encoded" >> {
        Json.prettyPrint(json) must be equalTo
          """{
            |  "titles" : [ {
            |    "title" : "My title"
            |  } ],
            |  "creators" : [ {
            |    "creatorName" : "too, Me",
            |    "familyName" : "too",
            |    "givenName" : "Me"
            |  } ],
            |  "publisher" : "Materials Data Facility",
            |  "publicationYear" : "1776",
            |  "resourceType" : {
            |    "resourceType" : "Resource type",
            |    "resourceTypeGeneral" : "Dataset"
            |  }
            |}""".stripMargin
      }
    }
  }

  "Given the DOI is provided">> {
    "When I generate JSON" >> {
      val json = DataCite.dataCiteWrites.writes(new DataCite(Seq("My title"),
        Seq("Me too"), None, None, Some(1776), None, None, Some("123-456"), None))
      "Then I should see the description property encoded" >> {
        Json.prettyPrint(json) must be equalTo
          """{
            |  "titles" : [ {
            |    "title" : "My title"
            |  } ],
            |  "creators" : [ {
            |    "creatorName" : "too, Me",
            |    "familyName" : "too",
            |    "givenName" : "Me"
            |  } ],
            |  "publisher" : "Materials Data Facility",
            |  "publicationYear" : "1776",
            |  "identifier" : {
            |    "identifier" : "123-456",
            |    "identifierType" : "DOI"
            |  }
            |}""".stripMargin
      }
    }
  }

  "Given related DOIs are provided">> {
    "When I generate JSON" >> {
      val json = DataCite.dataCiteWrites.writes(new DataCite(Seq("My title"),
        Seq("Me too"), None, None, Some(1776), None, None, None, Some(Seq("123","456"))))
      "Then I should see the description property encoded" >> {
        Json.prettyPrint(json) must be equalTo
          """{
            |  "titles" : [ {
            |    "title" : "My title"
            |  } ],
            |  "creators" : [ {
            |    "creatorName" : "too, Me",
            |    "familyName" : "too",
            |    "givenName" : "Me"
            |  } ],
            |  "publisher" : "Materials Data Facility",
            |  "publicationYear" : "1776",
            |  "relatedIdentifiers" : [ {
            |    "relatedIdentifier" : "123",
            |    "relatedIdentifierType" : "DOI",
            |    "relationType" : "IsPartOf"
            |  }, {
            |    "relatedIdentifier" : "456",
            |    "relatedIdentifierType" : "DOI",
            |    "relationType" : "IsPartOf"
            |  } ]
            |}""".stripMargin
      }
    }
  }

  private def generateJsonForCreator(aCreator: String):JsValue = {
    (DataCite.dataCiteWrites.writes(new DataCite(Seq("My title"),
      Seq(aCreator), None, None, None, None, None, None, None)
    )\"creators")(0)
  }

}
