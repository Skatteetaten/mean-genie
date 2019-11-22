package no.skatteetaten.aurora.mean.genie

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.support.expected
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import no.skatteetaten.aurora.mean.genie.service.createGetSchemaResultJson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@SpringBootTest
class MeanGenieIntegrationTest {
    private val openshift = MockWebServer()
    private val openshiftListener = object : WebSocketListener() {
        var webSocket: WebSocket? = null

        override fun onOpen(ws: WebSocket, response: Response) {
            webSocket = ws
        }
    }

    private val dbh = MockWebServer()

    init {
        openshift.enqueue(MockResponse().withWebSocketUpgrade(openshiftListener))
        openshift.start("openshift".port())

        dbh.enqueue(
            MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(createGetSchemaResultJson("123"))
        )

        dbh.enqueue(
            MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(""" {}""")
        )

        dbh.enqueue(
            MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(createGetSchemaResultJson("234"))
        )

        dbh.enqueue(
            MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(""" {}""")
        )

        dbh.start("dbh".port())
    }

    @AfterEach
    fun tearDown() {
        openshift.shutdown()
        dbh.shutdown()
    }

    @Test
    fun `Receive deleted event and call dbh`() {
        val webSocket = await untilNotNull { openshiftListener.webSocket }
        val json = """{
            "type" : "DELETED",
            "object": {
              "metadata" : {
                "name" : "test-app", 
                "namespace" : "test-utv", 
                "labels" : {
                  "affiliation" : "test"
                }
              },
              "spec": {
                "databases": ["123","234"] 
                } 
               } 
            }"""
        webSocket.send(json)

        dbh.assertThat()
            .containsRequest(GET, "/api/v1/schema/123")
            .containsRequest(GET, "/api/v1/schema/234")
            .containsRequest(DELETE, "/api/v1/schema/123")
            .containsRequest(DELETE, "/api/v1/schema/234")
    }

    @Test
    fun `Receive deleted event without databases`() {
        val webSocket = await untilNotNull { openshiftListener.webSocket }
        webSocket.send(""" { "type":"DELETED", "object": { "spec": { "databases": [] } } } """)

        dbh.assertThat().isEmpty()
    }

    private fun String.port(): Int {
        val yaml = ClassPathResource("application.yaml").file.readText()
        val values = ObjectMapper(YAMLFactory()).readTree(yaml)
        return values.at("/integrations/$this/port").asInt()
    }

    private fun MockWebServer.assertThat(): Assert<List<RecordedRequest>> {
        val requests = mutableListOf<RecordedRequest>()
        do {
            val request = this.takeRequest(500, TimeUnit.MILLISECONDS)?.let {
                requests.add(it)
            }
        } while (request != null)
        return assertThat(requests)
    }

    private fun Assert<List<RecordedRequest>>.containsRequest(method: HttpMethod, path: String): Assert<List<RecordedRequest>> =
        transform { requests ->
            if (requests.any { it.method == method.name && it.path == path }) {
                requests
            } else {
                expected("${method.name} request with $path but was $requests")
            }
        }
}
