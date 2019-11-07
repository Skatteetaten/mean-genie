package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.kotlin.core.publisher.toFlux

@Service
class DatabaseService(val webClient: WebClient, val sharedSecretReader: SharedSecretReader) {

    fun deleteSchemaByID(databases: List<String>) {
        databases.toFlux().flatMap { databaseId ->
            webClient
                .delete()
                .uri("/api/v1/schema/$databaseId")
                .header(HttpHeaders.AUTHORIZATION, "aurora-token ${sharedSecretReader.secret}")
                .retrieve()
                .bodyToMono<JsonNode>()
        }
    }
}
