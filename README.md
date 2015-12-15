# Gatling-gen

##### Testing web APIs using _[Gatling](http://gatling.io)_.

_Gatling-Gen_ (__GG__) is a _[Gatling](http://gatling.io)_ based tool for testing web services. It can test functionality, assert a _Service Level Agreement_ is being kept, and help you discover an optimal _SLA_.

Instead of writing Gatling scenarios, HTTP request defaults, user injections, and other low-level Gatling entities; you define the API as a set of strongly-typed endpoints. Then you define the functional and performance requirements that must be kept by these endpoints. Finally you hand the spec over to Gatlin-Gen.

_GG_ will then generate everything Gatling requires for running the test: the scenarios with their definitions of HTTP requests, the test input feeds required for data coverage, and finally a complete Gatling simulation ready for execution.

For debugging, or for running these experiments outside of Gatling, you can generate JSON output, instead of over-the-wire HTTP requests. This JSON will include all the web API test cases that Gatling would have run for such an API.

### Implementation

###### Request generation: from web API to bytes on the wire

It is helpful to understand the flow from a high-level web API spec, down to bytes on the wire. Here is the flow in reverse, as if we were watching a film backwards.

The learning curve going up from the _known_ HTTP requests to the _unknown_ high-level _GG_ abstractions, should be more gradual than the reverse route, as you are probably more
familiar with the first of the two. Request generation, viewed bottom-up _physical_ up to _application_ layer:

1. **Down the wire** goes he request, or perhaps into a string for debugging.
1. **HTTP protocol** is handled by _Ning_, the async HTTP client used by Gatling. It marshals the HTTP request object it was given by Gatling, and streams it down a connection to the host & port defined in the request.
1. **Gatling** builders prepare a`com.ning.http.client.Request` from the protocol
 and request builders it received from _GG_. These two objects encapsulate all that is required to describe every conceivable HTTP request- from `Accept` header to `URL`.
1. **Gatling-Gen driver** package take the HTTP request definition pushed down from the _webapi_ package, and convert it into Gatling protocol and request builders.
1. **Objects in the webapi** package receive the definitions of web services, endpoints, scenarios, and anything else specified by the tester that can influence the request generated. From these it compiles an HTTP request definition for each endpoint.
1. **Testers** use abstractions from the _webapi_ package to model web services and their endpoints. An endpoint definition encapsulates everything required to test the endpoint.

Test input-data, user and scenario definitions, and other GG features, all take a similar
path to the one taken by the HTTP request described above.

###### Load profile generation: from SLA to throttled injectors

Gatling runs a scenario under a load profile. This load profile is defined by some
function `time → virtual user injection`, called an _Injection Profile_. Gatling also
includes control elements for loops and branching, and elements for delays and
throttling in its test DSL.

These are used by _GG_ to validate an SLA, and to create experiments that can help
discover the optimal SLA.

For example, to assert the SLA `response time ≤ 100ms for 200rps for 95% of requests,
response time ≤ 500ms for 99.9% of requests`, all you have to do is encode the SLA in a table. A table being the most natural way to represent an SLA, this should be
easy.

_GG_ will then create the correct Gatling abstractions, and run a set of experiments to
validate the SLA. At the end of this process, you get the SLA table with its cells
painted green and red according the the experiment results.

###### Scala Packages

* The [webapi](tree/master/src/main/scala/com/maxmind/gatling/gen/webapi) package is for
defining web APIs.

* [sla](tree/master/src/main/scala/com/maxmind/gatling/gen/sla) is for discovering and setting _SLAs_.

* The [gen](tree/master/src/main/scala/com/maxmind/gatling/gen/gen) package is about test
input data generation, and for abstractions related to test input-data coverage.

* The [experiment](tree/master/src/main/scala/com/maxmind/gatling/gen/experiment) package
helps in configuring and running experiment.

* The [driver](tree/master/src/main/scala/com/maxmind/gatling/gen/experiment) package
is responsible for driving Gatling.

* Inside the [dev](tree/master/src/main/scala/com/maxmind/gatling/gen/dev) package you will find some dev tools and examples.


