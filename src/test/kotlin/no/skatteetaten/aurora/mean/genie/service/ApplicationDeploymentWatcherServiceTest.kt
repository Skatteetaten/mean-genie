package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

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

        every { databaseService.getSchemaById("123") } returns createMockSchemaRequest("123").toMono()
        every { databaseService.getSchemaById("234") } returns createMockSchemaRequest("234").toMono()

        // TODO: Should this mock return be better so that we can assert below
        every { databaseService.deleteSchemaByID("123") } returns jacksonObjectMapper().readTree("""{}""").toMono()
        every { databaseService.deleteSchemaByID("234") } returns jacksonObjectMapper().readTree("""{}""").toMono()

        val json = """{
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
        val jsonNode = jacksonObjectMapper().readTree(json)
        val response = applicationDeploymentWatcherService.deleteSchemasIfExists(jsonNode)
        val databases = response.timeout(Duration.ofSeconds(1)).toIterable().toList()
        assertThat(databases.size).isEqualTo(2)
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

fun createMockSchemaRequestString(id: String, type: String = "MANAGED"): String {
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
