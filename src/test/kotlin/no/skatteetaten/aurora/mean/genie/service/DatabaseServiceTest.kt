package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.skatteetaten.aurora.mean.genie.ApplicationConfig
import no.skatteetaten.aurora.mockmvc.extensions.mockwebserver.execute
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.web.reactive.function.client.WebClient

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DatabaseServiceTest {
    private val server = MockWebServer()
    private val url = server.url("/").toString()
    private val jsonBody = """{"status":"OK","totalCount":0,"items":[]}"""
    private val sharedSecretReader = mockk<SharedSecretReader> {
        every { secret } returns "abc123"
    }
    private val applicationConfig = ApplicationConfig("mean-genie", "123", sharedSecretReader)

    private val webClient =
        applicationConfig.webClientDbh(
            applicationConfig.dbhTcpClientWrapper(1000, 1000, 1000, null), url,
            WebClient.builder()
        )
    private val databaseService = DatabaseService(webClient, 50, 100, 3)

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `Verify that getSchema returns body `() {
        val request = server.execute(200 to createGetSchemaResultJson("123")) {
            val jsonResponse = runBlocking { databaseService.getSchemaById("123") }
            assertThat(jsonResponse?.id).isEqualTo("123")
        }
        assertThat(request.first()).isNotNull()
    }

    @Test
    fun `Verify that getSchema for non existing schame does not throw`() {
        val request = server.execute(404 to """{}""") {
            val jsonResponse = runBlocking { databaseService.getSchemaById("123") }
            assertThat(jsonResponse).isNull()
        }
        assertThat(request.first()).isNotNull()
    }

    @Test
    fun `Verify that deleteSchemaById returns body that is not null`() {
        val request = server.execute(200 to jsonBody) {
            val jsonResponse = runBlocking { databaseService.deleteSchemaById("123") }
            assertThat(jsonResponse).isNotNull()
        }
        assertThat(request.first()).isNotNull()
    }

    @Test
    fun `Verify that deleteSchemaByID should not retry not authenticated error`() {
        val request = server.execute(401 to jsonBody) {
            assertThat {
                runBlocking { databaseService.deleteSchemaById("123") }
            }.isFailure()
        }
        assertThat(request.first()).isNotNull()
    }

    @Test
    fun `Verify that deleteSchemaByID should retry 503 error`() {
        val request = server.execute(503 to jsonBody, 200 to jsonBody) {
            val jsonResponse = runBlocking { databaseService.deleteSchemaById("123") }
            assertThat(jsonResponse).isNotNull()
        }
        assertThat(request.size).isEqualTo(2)
    }

    @Test
    fun `Verify that deleteSchemaByID should retry 503 error 3 times`() {
        val request = server.execute(503 to jsonBody, 503 to jsonBody, 503 to jsonBody) {
            runBlocking {
                assertThat {
                    databaseService.deleteSchemaById("123")
                }.isFailure()
            }
        }
        assertThat(request.size).isEqualTo(3)
    }
}
