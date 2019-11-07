package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ApplicationDeploymentWatcherService(
    @Value("\${integrations.operations.scope:#{null}}") val operationScopeConfiguration: String?,
    val watcher: KubernetesWatcher,
    val databaseService: DatabaseService
) {
    @Async
    fun watch() {
        val labelSelector = if (operationScopeConfiguration == null) {
            "!operationScope"
        } else {
            "operationScope=$operationScopeConfiguration"
        }
        val url = "/apis/skatteetaten.no/v1/applicationdeployments?watch=true&labelSelector=$labelSelector"
        watcher.watch(url, listOf("DELETED")) { event ->
            val jsonArray = event.at("/object/spec/databases") as ArrayNode
            val databases = jsonArray.map { it.textValue() }

            if (databases.isNotEmpty()) {
                databaseService.deleteSchemaByID(databases)
                logger.debug { "Deleted schema $databases" }
            }
        }
    }
}
