package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.retry.Retry

private val logger = KotlinLogging.logger {}

@Service
class DatabaseService(
    val webClient: WebClient,
    @Value("\${integrations.dbh.retry.delay.min:100}") val retryMinDelay: Long,
    @Value("\${integrations.dbh.retry.delay.maxy:2000}") val retryMaxDelay: Long,
    @Value("\${integrations.dbh.retry.times:3}") val retryTimes: Long
) {

    suspend fun deleteSchemaByID(databaseId: String): JsonNode {
        return webClient
            .delete()
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono<JsonNode>()
            .retryWithLog(retryMinDelay, retryMaxDelay, retryTimes)
            .awaitFirst()
    }

    suspend fun getSchemaById(databaseId: String): DatabaseResult? {
        return webClient
            .get()
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono<JsonNode>()
            .onErrorResume(WebClientResponseException.NotFound::class.java) {
                Mono.empty()
            }
            .retryWithLog(retryMinDelay, retryMaxDelay, retryTimes)
            .map { it.toDatabaseResponse() }
            .awaitFirstOrNull()
    }

    private fun JsonNode.toDatabaseResponse(): DatabaseResult {
        val databaseType: String = this.at("/items/0/type").textValue()
        val id: String = this.at("/items/0/id").textValue()
        val labelValues: Map<String, String> =
            jacksonObjectMapper().convertValue(this.at("/items/0/labels"))
        val databaseLabels = labelValues.filter { (key, _) ->
            key != "userId" && key != "name"
        }
        return DatabaseResult(databaseType, id, databaseLabels)
    }

    fun <T> Mono<T>.retryWithLog(retryFirstInMs: Long, retryMaxInMs: Long, retryTimes: Long = 3): Mono<T> {
        if (retryTimes == 0L) {
            logger.debug("Do not retry")
            return this
        }

        return this.retryWhen(Retry.onlyIf<Mono<T>> {
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
            } else {
                logger.debug("retrying message=${it.exception().message}")
            }

            it.exception() !is WebClientResponseException.Unauthorized
        }.exponentialBackoff(Duration.ofMillis(retryFirstInMs), Duration.ofMillis(retryMaxInMs)).retryMax(retryTimes))
    }
}

data class DatabaseResult(val type: String, val id: String, val labels: Map<String, String>)
