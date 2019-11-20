package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class DatabaseService(val webClient: WebClient) {

    suspend fun deleteSchemaByID(databaseId: String): JsonNode {
        return webClient
            .delete()
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono<JsonNode>()
            .awaitFirst()
    }

    suspend fun getSchemaById(databaseId: String): DatabaseResult {
        return webClient
            .get()
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono<JsonNode>().map { jsonNode ->
                val databaseType: String = jsonNode.at("/items/0/type").textValue()
                val databaseId: String = jsonNode.at("/items/0/id").textValue()
                val labelValues: Map<String, String> =
                    jacksonObjectMapper().convertValue(jsonNode.at("/items/0/labels"))
                val databaseLabels = labelValues.filter { (key, _) ->
                    key != "userId" && key != "name"
                }
                DatabaseResult(databaseType, databaseId, databaseLabels)
            }.awaitFirst()
    }
}

data class DatabaseResult(val type: String, val id: String, val labels: Map<String, String>)
