package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class DatabaseService(val webClient: WebClient) {

    fun deleteSchemaByID(databaseId: String): Mono<JsonNode> {
        return webClient
            .delete()
            .uri("/api/v1/schema/$databaseId")
            .retrieve()
            .bodyToMono<JsonNode>()
    }

    fun getSchemaById(databaseId: String): Mono<JsonNode> {
        return webClient
            .get()
            .uri("/api/v1/schema/$databaseId")
            .retrieve()
            .bodyToMono()
    }
}
