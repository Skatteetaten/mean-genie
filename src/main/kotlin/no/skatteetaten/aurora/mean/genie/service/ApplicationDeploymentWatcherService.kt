package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

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
            deleteSchemasIfExists(event)
        }
    }

    fun checkForOperationScopeLabel(): String {
        return if (operationScopeConfiguration.isNullOrEmpty()) {
            "!operationScope"
        } else {
            "operationScope=$operationScopeConfiguration"
        }
    }

    fun deleteSchemasIfExists(event: JsonNode): Mono<Void> {
        val adLabels = mapOf(
            "environment" to event.at("/object/metadata/namespace").textValue(),
            "application" to event.at("/object/metadata/name").textValue(),
            "affiliation" to event.at("/object/metadata/labels/affiliation").textValue()
        )

        val jsonArray = event.at("/object/spec/databases") as ArrayNode
        val databases = jsonArray.map { it.textValue() }

        return if (databases.isEmpty()) {
            Mono.empty()
        } else {
            logger.debug { "Attempting to delete database schema $databases" }
            databaseService.getSchemaById(databases)
                .log()
                .filter {

                    val item: JsonNode = it.at("/items/0")
                    val type = item["type"].textValue()

                    if(type == "EXTERNAL") {
                        false
                    } else {

                        val labelValues: Map<String, String> = jacksonObjectMapper()
                            .convertValue(item["labels"])

                        val schemaLabels = labelValues.filter { (key, value) ->
                            key != "userId" && key != "name"
                        }
                         schemaLabels == adLabels

                    }

                }
                .log()
                .map {
                    databaseService.deleteSchemaByID(databases)
                }
                .then()
        }
    }
}
