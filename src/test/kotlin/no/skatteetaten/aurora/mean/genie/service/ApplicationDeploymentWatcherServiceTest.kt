package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ApplicationDeploymentWatcherServiceTest {

    private val databaseService = mockk<DatabaseService>()
    private val kubernetesWatcher = mockk<KubernetesWatcher>()

    private val applicationDeploymentWatcherService =
        ApplicationDeploymentWatcherService("", kubernetesWatcher, databaseService)

    @Test
    fun `Check if operatioScope label is not set`() {
        val operationScope = applicationDeploymentWatcherService.checkForOperationScopeLabel()
        assertThat(operationScope).isEqualTo("!operationScope")
    }

    @Test
    fun `Check if operatioScope label is set to dev`() {
        val applicationDeploymentWatcherServiceWithOperationScope =
            ApplicationDeploymentWatcherService("dev", kubernetesWatcher, databaseService)
        val operationScope = applicationDeploymentWatcherServiceWithOperationScope.checkForOperationScopeLabel()
        assertThat(operationScope).isEqualTo("operationScope=dev")
    }

    @Test
    fun `Set database to cooldown if found`() {

        coEvery { databaseService.getSchemaById("123") } returns createMockSchemaRequest("123")
        coEvery { databaseService.deleteSchemaByID("123") } returns jacksonObjectMapper().readTree("""{}""")

        val database = runBlocking { applicationDeploymentWatcherService.handleDeleteDatabaseSchema("123", mapOf(
            "affiliation" to "test",
            "application" to "test-app",
            "environment" to "test-utv"
        )) }
        assertThat(database).isNotNull()
    }

    @Test
    fun `ignore database with wrong labels`() {

        coEvery { databaseService.getSchemaById("123") } returns createMockSchemaRequest("123")

        val database = runBlocking {
            applicationDeploymentWatcherService.handleDeleteDatabaseSchema(
                "123", mapOf(
                    "affiliation" to "test2",
                    "application" to "test-app",
                    "environment" to "test-utv"
                )
            )
        }
        assertThat(database).isNull()
    }

    @Test
    fun `ignore database that is external`() {

        coEvery { databaseService.getSchemaById("123") } returns createMockSchemaRequest("123", "EXTERNAL")

        val database = runBlocking {
            applicationDeploymentWatcherService.handleDeleteDatabaseSchema(
                "123", mapOf(
                    "affiliation" to "test",
                    "application" to "test-app",
                    "environment" to "test-utv"
                )
            )
        }
        assertThat(database).isNull()
    }
}

fun createMockSchemaRequest(id: String, type: String = "MANAGED"): DatabaseResult {

    return DatabaseResult(
        type, id, mapOf(
            "application" to "test-app",
            "environment" to "test-utv",
            "affiliation" to "test"
        )
    )
}

fun createGetSchemaResultJson(id: String, type: String = "MANAGED"): String {
    return """
            {
              "items": [
                {
                 "type" : "$type",
                 "id" : "$id",
                 "labels" : {
                   "userId" : "hero",
                   "name" : "application-database", 
                   "application" : "test-app",
                   "environment" : "test-utv", 
                   "affiliation" : "test"
                 }
               }
               ]
            }
        """.trimIndent()
}
