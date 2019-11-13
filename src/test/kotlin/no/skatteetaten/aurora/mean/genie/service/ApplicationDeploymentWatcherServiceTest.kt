package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import org.junit.jupiter.api.Test

class ApplicationDeploymentWatcherServiceTest {

    private val databaseService = mockk<DatabaseService>(relaxed = true)
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
        val json = """{"object": {"spec": {"databases": ["123","234"] } } }"""
        val jsonNode = jacksonObjectMapper().readTree(json)
        val response = applicationDeploymentWatcherService.deleteSchemasIfExists(jsonNode)
        response.block(Duration.ofSeconds(3))
        verify { databaseService.deleteSchemaByID(listOf("123", "234")) }
    }
}
