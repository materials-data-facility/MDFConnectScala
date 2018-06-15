package org.materialsdatafacility.connect.models

import org.specs2.mutable._
import play.api.libs.json._

class ConvertRequestSpec extends Specification {
  "Given a Convert Request" >> {
    val dc = new DataCite(Seq("My title"), Seq("Bob Dobbo"), None, None, Some(1984), None, None, None, None)
    val convertRequest = new ConvertRequest(dc, Seq("http://foo.com/bar", "http://bar.com/foo"), Some(false))

    "When I write the JSON" >> {
      val json = ConvertRequest.convertRequestWrites.writes(convertRequest)
      "Then it should be formatted correctly" >>{
        val result = Json.prettyPrint(json)
        result must be equalTo(
          """{
            |  "dc" : {
            |    "titles" : [ "My title" ],
            |    "creators" : [ {
            |      "creatorName" : "Dobbo, Bob",
            |      "familyName" : "Dobbo",
            |      "givenName" : "Bob"
            |    } ],
            |    "publisher" : "Materials Data Facility",
            |    "publicationYear" : "1984"
            |  },
            |  "data" : [ "http://foo.com/bar", "http://bar.com/foo" ],
            |  "test" : false
            |}""".stripMargin
        )
      }
    }
  }

  "Given a Convert Request that doesn't specify the test setting" >> {
    val dc = new DataCite(Seq("My title"), Seq("Bob Dobbo"), None, None, Some(1984), None, None, None, None)
    val convertRequest = new ConvertRequest(dc, Seq("http://foo.com/bar"), None)

    "When I write the JSON" >> {
      val json = ConvertRequest.convertRequestWrites.writes(convertRequest)
      "Then it should default to true" >>{
        val result = Json.prettyPrint(json)
        result must be equalTo(
          """{
            |  "dc" : {
            |    "titles" : [ "My title" ],
            |    "creators" : [ {
            |      "creatorName" : "Dobbo, Bob",
            |      "familyName" : "Dobbo",
            |      "givenName" : "Bob"
            |    } ],
            |    "publisher" : "Materials Data Facility",
            |    "publicationYear" : "1984"
            |  },
            |  "data" : [ "http://foo.com/bar" ],
            |  "test" : true
            |}""".stripMargin
          )
      }
    }
  }


}
