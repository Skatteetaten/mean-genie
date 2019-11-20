package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

    val JsonNode.namespace: String get() = this.at("/object/metadata/namespace").textValue()
    val JsonNode.name: String get() = this.at("/object/metadata/name").textValue()
    val JsonNode.affiliation: String get() = this.at("/object/metadata/labels/affiliation").textValue()
    val JsonNode.databases: List<String>
        get() {
            val jsonArray = this.at("/object/spec/databases") as ArrayNode
            return jsonArray.map { it.textValue() }
        }
    val JsonNode.databaseType: String get() = this.at("/items/0/type").textValue()
    val JsonNode.databaseId: String get() = this.at("/items/0/id").textValue()
    val JsonNode.databaseLabels: Map<String, String>
        get() {
            val labelValues: Map<String, String> = jacksonObjectMapper().convertValue(this.at("/items/0/labels"))
            return labelValues.filter { (key, _) ->
                key != "userId" && key != "name"
            }
        }

    fun deleteSchemasIfExists(event: JsonNode): Flux<JsonNode> {
        val adLabels = mapOf(
            "environment" to event.namespace,
            "application" to event.name,
            "affiliation" to event.affiliation
        )

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
                it.databaseType != "EXTERNAL" && it.databaseLabels == adLabels
            }.flatMap {
                databaseService.deleteSchemaByID(it.databaseId)
            }.log()
    }
}
