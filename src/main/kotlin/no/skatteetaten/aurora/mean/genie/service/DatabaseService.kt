package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class DatabaseService(val webClient: WebClient) {

    fun deleteSchemaByID(databaseId: String): Mono<JsonNode> {
        return webClient
            .delete()
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono()
    }

    fun getSchemaById(databaseId: String): Mono<DatabaseResult> {
        return webClient
            .get()
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono<JsonNode>().map {
                DatabaseResult(it)
            }
    }
}

class DatabaseResult(private val jsonNode: JsonNode) {
    val databaseType: String get() = jsonNode.at("/items/0/type").textValue()
    val databaseId: String get() = jsonNode.at("/items/0/id").textValue()
    val databaseLabels: Map<String, String>
        get() {
            val labelValues: Map<String, String> = jacksonObjectMapper().convertValue(jsonNode.at("/items/0/labels"))
            return labelValues.filter { (key, _) ->
                key != "userId" && key != "name"
            }
        }
}
