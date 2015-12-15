

# The `webapi` package

The `com.maxmind.galing.gen.webapi` package is used to define web services, mostly for testing them.

**Gatling-Gen** works in two stages: First you define the web API, then you generate and run experiments to test its functionality. These are also the definitions used to test the web API under stress, and to test web service performance SLAs. The first stage is handled by this package.

**Configuring routing** for a web application is almost the same problem. In both cases you need to describe the same entities: web services, endpoints, and actions. Both define an action as a adapter to some server function, adapting its parameters & return values for web service usage. Adapting a function for web usage is about translating the parameters & return values from & to HTTP messages, where they are encoded in query parameters, path segments, headers, body multipart or JSON data, or any of the multitude of other ways people encode data in HTTP.

The only difference is that routing libraries use this definition to expose the adapted functions to web clients, while here we use the the definitions to generate tests.

### What is a web service?

**A web service** is composed of _endpoints_ each with its own URL, and these in turn are made up of _actions_, each with its own HTTP method. Each level can add its own layer of defaults to the HTTP requests. The web service could add a required authentication method common to all incoming requests, for example, or the endpoint could add some required fragment to the end of the path.

`Action` objects define the HTTP Requests:

* Method
* URL = scheme + host + port + path + query params + fragment
* Headers and cookies
* Body
* Authentication format details
* Metadata for requests analysis: a hash for identification, a name, label, description, and other correlation and categorization aides required

**Captures**

### Types

1. `Req` - a request definition ready to be shipped to the Gatling driver component. Gatling requires two objects to describe an HTTP request: an `HTTPRequestBuilder` and an `HTTPProtocolBuidler`. This is what a `Req` holds.
1. `WebService` - the goal of the tester is to define on of these. It is built from `Acion`s grouped into `Endpoint`s.


1. `Path` `Segment` ◃ `Path` `Tq` & `Tp` - type params for the request & response API types respectively `Printer` & `Parser`
