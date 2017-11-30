package io.jgille.spockmockserver.blackbox

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.mockserver.client.server.MockServerClient
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class ServiceUnderTestSpec extends Specification {

    @Shared
    private RESTClient serviceClient

    @Shared
    private MockServerClient mockServer

    def setupSpec() {
        serviceClient = new RESTClient("http://localhost:8282", ContentType.JSON)
        mockServer = new MockServerClient("localhost", 8383)
    }

    def setup() {
        mockServer.reset()
    }

    def "An event is posted to the backend when issuing a greet request"() {
        given: "That the backend service accepts the greeting"

        def req = HttpRequest.request()
                .withMethod("POST")
                .withPath("/events")

        def res = HttpResponse.response()
                .withStatusCode(200)
                .withHeaders(
                new Header("Content-Type", "application/json; charset=utf-8"),
                new Header("Correlation-Id", "cid"),
        )

        mockServer.when(req).respond(res)

        when: "Posting a greeting"

        def response = serviceClient.post(
                path: "/greet",
                headers: [
                        "Correlation-Id": "cid"
                ],
                body: [greeting: "Hello from Spock"]
        )

        then: "The request is successful"

        response.status == 200

        and: "The greeting was posted to the backend"

        mockServer.verify(req)
    }

    def "Make sure things fail as expected when the backend request times out"() {
        given:
        def req = HttpRequest.request()
                .withMethod("POST")
                .withPath("/events")

        def res = HttpResponse.response()
                .withDelay(TimeUnit.SECONDS, 3)
                .withStatusCode(200)
                .withHeaders(
                new Header("Content-Type", "application/json; charset=utf-8"),
                new Header("Correlation-Id", "cid"),
        )

        mockServer.when(req).respond(res)

        when:
        serviceClient.post(
                path: "/greet",
                headers: [
                        "Correlation-Id": "cid"
                ],
                body: [greeting: "Hello from Spock"]
        )

        then: "The request is successful"
        def e = thrown(HttpResponseException)
        e.statusCode == 503
    }
}
