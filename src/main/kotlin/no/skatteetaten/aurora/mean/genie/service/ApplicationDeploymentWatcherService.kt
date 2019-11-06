package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Service
class ApplicationDeploymentWatcherService(
    @Value("\${integrations.operations.scope:#{null}}") val operationScopeConfiguration: String?,
    val watcher: KubernetesWatcher,
    val databaseService: DatabaseService
) {
    fun watch() {
        val labelSelector = if (operationScopeConfiguration == null) {
            "!operationScope"
        } else {
            "operationScope=$operationScopeConfiguration"
        }
        logger.info { operationScopeConfiguration }

        val url = "/apis/skatteetaten.no/v1/applicationdeployments?watch=true&labelSelector=$labelSelector"
        watcher.watch(url, listOf("DELETED")) { event ->
            val jsonArray = event.at("/object/spec/databases") as ArrayNode
            val databases = jsonArray.map { it.textValue() }

            if (databases.isNotEmpty()) {
                databaseService.deleteSchemaByID(databases).then()
                logger.debug { "Deleted schema $databases" }
            }
            Mono.empty()
        }
    }
}
