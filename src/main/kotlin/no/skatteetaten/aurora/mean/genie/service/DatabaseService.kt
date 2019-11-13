package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

@Service
class DatabaseService(val webClient: WebClient) {

    fun deleteSchemaByID(databases: List<String>): Flux<JsonNode> {
        return databases.toFlux().flatMap { databaseId ->
            webClient
                .delete()
                .uri("/api/v1/schema/$databaseId")
                .retrieve()
                .bodyToMono<JsonNode>()
                .log()
        }
    }
}
