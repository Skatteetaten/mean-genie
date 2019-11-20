package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

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
            deleteSchemasIfExists(event).then()
        }
    }

    fun checkForOperationScopeLabel(): String {
        return if (operationScopeConfiguration.isNullOrEmpty()) {
            "!operationScope"
        } else {
            "operationScope=$operationScopeConfiguration"
        }
    }

    fun deleteSchemasIfExists(event: JsonNode): Flux<JsonNode> {

        val event = KubernetesDatabaseEvent(event)
        val databases = event.databases
        if (databases.isEmpty()) {
            return Flux.empty()
        }
        logger.debug { "Attempting to delete database schema $databases" }

        // TODO: Feilhåndtering. Retry. Hva hvis dbh feiler eller vi får feil.
        return databases.toFlux().flatMap {
            databaseService.getSchemaById(it)
        }.log()
            .filter {
                it.databaseType != "EXTERNAL" && it.databaseLabels == event.labels
            }.flatMap {
                databaseService.deleteSchemaByID(it.databaseId)
            }.log()
    }
}

// TODO, denne eller den under?
fun JsonNode.toKubernetesDatabaseEvent(): KubernetesDatabaseEvent2 {

    val labels = mapOf(
        "environment" to at("/object/metadata/namespace").textValue(),
        "application" to at("/object/metadata/name").textValue(),
        "affiliation" to at("/object/metadata/labels/affiliation").textValue()
    )

    val databases = (this.at("/object/spec/databases") as ArrayNode).map { it.textValue() }

    return KubernetesDatabaseEvent2(databases, labels)
}

data class KubernetesDatabaseEvent2(val databases: List<String>, val labels: Map<String, String>)

// TODO, er det bedre å lage objektet i en metode og få evt marshalling feil tidlig?
class KubernetesDatabaseEvent(val jsonNode: JsonNode) {
    val labels = mapOf(
        "environment" to namespace,
        "application" to name,
        "affiliation" to affiliation
    )

    val namespace: String get() = jsonNode.at("/object/metadata/namespace").textValue()
    val name: String get() = jsonNode.at("/object/metadata/name").textValue()
    val affiliation: String get() = jsonNode.at("/object/metadata/labels/affiliation").textValue()
    val databases: List<String>
        get() {
            val jsonArray = jsonNode.at("/object/spec/databases") as ArrayNode
            return jsonArray.map { it.textValue() }
        }
}