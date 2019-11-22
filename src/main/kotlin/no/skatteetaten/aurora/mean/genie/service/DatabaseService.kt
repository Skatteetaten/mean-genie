package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactive.awaitFirst
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.retry.Retry
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Service
class DatabaseService(
    val webClient: WebClient,
    @Value("\${integrations.dbh.retryMinDelay:100}") val retryMinDelay: Long,
    @Value("\${integrations.dbh.retryMaxDelay:2000}") val retryMaxDelay: Long
) {

    suspend fun deleteSchemaByID(databaseId: String): JsonNode {
        return webClient
            .delete()
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono<JsonNode>()
            .retryWithLog(retryMinDelay, retryMaxDelay)
            .awaitFirst()
    }

    suspend fun getSchemaById(databaseId: String): DatabaseResult {
        return webClient
            .get()
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono<JsonNode>()
            .retryWithLog(retryMinDelay, retryMaxDelay)
            .map { jsonNode ->
                val databaseType: String = jsonNode.at("/items/0/type").textValue()
                val id: String = jsonNode.at("/items/0/id").textValue()
                val labelValues: Map<String, String> =
                    jacksonObjectMapper().convertValue(jsonNode.at("/items/0/labels"))
                val databaseLabels = labelValues.filter { (key, _) ->
                    key != "userId" && key != "name"
                }
                DatabaseResult(databaseType, id, databaseLabels)
            }
            .awaitFirst()
    }

    fun <T> Mono<T>.retryWithLog(retryFirstInMs: Long, retryMaxInMs: Long, retryTimes: Long = 3): Mono<T> {
        if (retryTimes == 0L) {
            logger.debug("Do not retry")
            return this
        }
        return this.retryWhen(Retry.onlyIf<Mono<T>> {
            // TODO: Burde vi logge litt hver gang? mulighet for å skru av i test?
            if (it.iteration() == retryTimes) {
                logger.info {
                    val e = it.exception()
                    val msg = "Retrying failed request, ${e.message}"
                    if (e is WebClientResponseException) {
                        "message=$msg, method=${e.request?.method} uri=${e.request?.uri} code=${e.statusCode}"
                    } else {
                        msg
                    }
                }
            }

            it.exception() !is WebClientResponseException.Unauthorized
        }.exponentialBackoff(Duration.ofMillis(retryFirstInMs), Duration.ofMillis(retryMaxInMs)).retryMax(retryTimes))
    }
}

data class DatabaseResult(val type: String, val id: String, val labels: Map<String, String>)
