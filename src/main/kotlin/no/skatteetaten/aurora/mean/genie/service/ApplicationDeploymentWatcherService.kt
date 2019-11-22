package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ApplicationDeploymentWatcherService(
    @Value("\${integrations.operations.scope:}") val operationScopeConfiguration: String?,
    val watcher: KubernetesWatcher,
    val databaseService: DatabaseService
) {
    @Async
    fun watch() {
        val labelSelector = checkForOperationScopeLabel()
        val url = "/apis/skatteetaten.no/v1/applicationdeployments?watch=true&labelSelector=$labelSelector"
        watcher.watch(url, listOf("DELETED")) { event ->
            val dbhEvent = event.toKubernetesDatabaseEvent()
            dbhEvent.databases.forEach {
                try {
                    handleDeleteDatabaseSchema(it, dbhEvent.labels)
                } catch (e: Exception) {
                    logger.info("Failed deleting database with id=$it", e)
                }
            }
        }
    }

    suspend fun handleDeleteDatabaseSchema(id: String, labels: Map<String, String>): JsonNode? {
        logger.debug { "Handle schema with with id=$id labels=$labels" }
        val dbhResult = databaseService.getSchemaById(id) ?: return null

        logger.debug { "Found schema with details $dbhResult" }
        return if (dbhResult.type != "EXTERNAL" && dbhResult.labels == labels) {
            databaseService.deleteSchemaByID(dbhResult.id).also {
                logger.info { "Deleted schema with id=$id" }
            }
        } else null
    }

    fun checkForOperationScopeLabel(): String {
        return if (operationScopeConfiguration.isNullOrEmpty()) {
            "!operationScope"
        } else {
            "operationScope=$operationScopeConfiguration"
        }
    }
}

fun JsonNode.toKubernetesDatabaseEvent(): KubernetesDatabaseEvent {

    val labels = mapOf(
        "environment" to at("/object/metadata/namespace").textValue(),
        "application" to at("/object/metadata/name").textValue(),
        "affiliation" to at("/object/metadata/labels/affiliation").textValue()
    )

    val databases = (this.at("/object/spec/databases") as ArrayNode).map { it.textValue() }

    return KubernetesDatabaseEvent(databases, labels)
}

data class KubernetesDatabaseEvent(val databases: List<String>, val labels: Map<String, String>)
