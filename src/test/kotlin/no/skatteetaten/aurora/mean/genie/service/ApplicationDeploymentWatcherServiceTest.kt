package no.skatteetaten.aurora.mean.genie.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.mockk
import org.junit.Test

class ApplicationDeploymentWatcherServiceTest {

    private val databaseService = mockk<DatabaseService>()
    private val kubernetesWatcher = mockk<KubernetesWatcher>()

    private val applicationDeploymentWatcherService =
        ApplicationDeploymentWatcherService(null, kubernetesWatcher, databaseService)

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
}