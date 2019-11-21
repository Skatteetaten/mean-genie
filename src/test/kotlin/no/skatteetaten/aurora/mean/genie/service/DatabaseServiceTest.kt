package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isNotNull
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.ApplicationConfig
import no.skatteetaten.aurora.mockmvc.extensions.mockwebserver.execute
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class DatabaseServiceTest {
    private val server = MockWebServer()
    private val url = server.url("/").toString()
    private val jsonBody = """{"status":"OK","totalCount":0,"items":[]}"""
    private val sharedSecretReader = mockk<SharedSecretReader> {
        every { secret } returns "abc123"
    }
    private val applicationConfig = ApplicationConfig("mean-genie", sharedSecretReader)

    private val webClient =
        applicationConfig.webClientDbh(
            applicationConfig.dbhTcpClientWrapper(1000, 1000, 1000, null), url
        )
    private val databaseService = DatabaseService(webClient)

    @Test
    fun `Verify that deleteSchemaByID returns body that is not null`() {
        val request = server.execute(200 to jsonBody) {
            val jsonResponse = runBlocking  {databaseService.deleteSchemaByID("123") }
            assertThat(jsonResponse).isNotNull()
        }
        logger.info { request.first()?.headers }
    }
}
