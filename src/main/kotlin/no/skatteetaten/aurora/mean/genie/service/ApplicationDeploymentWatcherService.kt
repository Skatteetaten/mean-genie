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
                handleDeleteDatabaseSchema(it, dbhEvent.labels)
            }
        }
    }

    suspend fun handleDeleteDatabaseSchema(it: String, labels: Map<String, String>): JsonNode? {
        val dbhResult = databaseService.getSchemaById(it) ?: return null

        return if (dbhResult.type != "EXTERNAL" && dbhResult.labels == labels) {
            databaseService.deleteSchemaByID(dbhResult.id)
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
