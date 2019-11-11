package no.skatteetaten.aurora.mean.genie

import assertk.Assert
import assertk.assertThat
import assertk.assertions.support.expected
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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import java.util.concurrent.TimeUnit

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
        openshift.start(8081)

        dbh.enqueue(
            MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody("{}")
        )
        dbh.start(8082)
    }

    @AfterEach
    fun tearDown() {
        openshift.shutdown()
        dbh.shutdown()
    }

    @Test
    fun `Received deleted event and call dbh`() {
        val webSocket = await untilNotNull { openshiftListener.webSocket }
        webSocket.send(""" { "type":"DELETED", "object": { "spec": { "databases": ["123", "234"] } } } """)

        val request1 = dbh.takeRequest(1, TimeUnit.SECONDS)
        assertThat(request1).deleteRequest("/api/v1/schema/123")
        val request2 = dbh.takeRequest(1, TimeUnit.SECONDS)
        assertThat(request2).deleteRequest("/api/v1/schema/234")
    }

    private fun Assert<RecordedRequest>.deleteRequest(path: String) = given {
        if (it.path == path && it.method == HttpMethod.DELETE.name) return
        expected("DELETE request with path $path but was ${it.path}")
    }
}
