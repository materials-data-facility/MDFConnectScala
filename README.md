# Scala Bindings for Materials Data Facility API
This library contains a service and associated case classes for interacting
with MDF's Connect service. This service is used to submit datasets
for ingesting into the [facility](https://materialsdatafacility.org).

# Prerequisites
In order to use this library you will require a client id and secret. This
can be obtained from [Globus](https://www.globus.org). You will then need to
contact the MDF team to grant your account permission to publish via the API.

We currently only support _confidential client_ authentication. This is
appropriate for a back-end service, however future versions of this library
should support the same OAuth flows exposed by the Python SDK.
 
This library uses the Scala Play WS module to perform all of the REST calls.

# How to Use
The `MDFConnectPublishService` offers two methods:
- `authenitcate` - Passes your client ID and secret to globus auth to receive
an access token.
- `submitConvertRequest` - Given an access token and a populated `ConvertRequest`
this method will submit it to the service and return a simple result string.

# Case Classes
This library contains case classes to represent the JSON objects used by Globus
auth and the MDF Connect service.  

## DataCite
This class encapsulates some of the basic data that can be represented by the
MDF Connect [dc schema](https://github.com/materials-data-facility/data-schemas/blob/master/dc.json). The underlying schema can represent a rich network of collaborators and their
affiliations. For now, this case class only represents the most basic cases and
should eventually be extended to handle more.

## ConvertRequest
This class contains all of the information required to submit a dataset to
MDF Connect for ingestion. In addition to a populated `DataCite` record, this
records the URL from which MDFConnect can download the zipped dataset, and
a boolean value which indicates if this is a test submission.
