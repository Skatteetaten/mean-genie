package no.skatteetaten.aurora.mean.genie

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isNull
import assertk.assertions.support.expected
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.util.concurrent.TimeUnit
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
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

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
                .setResponseCode(200)
                .setBody("{}")
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
        webSocket.send(""" { "type":"DELETED", "object": { "spec": { "databases": ["123", "234"] } } } """)

        val request1 = dbh.takeRequest(1, TimeUnit.SECONDS)
        assertThat(request1).isDeleteRequest("/api/v1/schema/123")
        val request2 = dbh.takeRequest(1, TimeUnit.SECONDS)
        assertThat(request2).isDeleteRequest("/api/v1/schema/234")
    }

    @Test
    fun `Receive deleted event without databases`() {
        val webSocket = await untilNotNull { openshiftListener.webSocket }
        webSocket.send(""" { "type":"DELETED", "object": { "spec": { "databases": [] } } } """)

        val request = dbh.takeRequest(1, TimeUnit.SECONDS)
        assertThat(request).isNull()
    }

    private fun String.port(): Int {
        val yaml = ClassPathResource("application.yaml").file.readText()
        val values = ObjectMapper(YAMLFactory()).readTree(yaml)
        return values.at("/integrations/$this/port").asInt()
    }

    private fun Assert<RecordedRequest>.isDeleteRequest(path: String) = given {
        if (it.path == path && it.method == HttpMethod.DELETE.name) return
        expected("DELETE request with path $path but was ${it.method} ${it.path}")
    }
}
