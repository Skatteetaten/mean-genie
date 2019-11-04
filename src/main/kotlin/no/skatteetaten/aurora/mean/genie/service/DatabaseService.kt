package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import no.skatteetaten.aurora.mean.genie.controller.security.SharedSecretReader
import no.skatteetaten.aurora.mean.genie.kubernetesObjectMapper
import no.skatteetaten.aurora.mean.genie.model.ApplicationDeployment
import no.skatteetaten.aurora.mean.genie.model.WatchEvent
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

@Service
class DatabaseService(val webClient: WebClient, val sharedSecretReader: SharedSecretReader) {

    fun deleteSchemaByID(databases: List<String>): Flux<JsonNode> {
        return databases.toFlux().flatMap { databaseId ->
            webClient
                .delete()
                .uri("/api/v1/schema/$databaseId")
                .header(HttpHeaders.AUTHORIZATION, "aurora-token ${sharedSecretReader.secret}")
                .retrieve()
                .bodyToMono<JsonNode>()
        }
    }

    private fun readWatchEventResource(json: JsonNode): ApplicationDeployment =
        kubernetesObjectMapper().convertValue<WatchEvent<ApplicationDeployment>>(json).resource

    fun readDatabaseLabel(json: JsonNode): List<String> = readWatchEventResource(json).spec.databases

    fun readOperationScopeLabel(json: JsonNode): String? =
        readWatchEventResource(json).metadata.labels["operationScope"]
}
