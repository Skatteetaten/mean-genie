package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.util.retry.Retry

private val logger = KotlinLogging.logger {}

@Service
class DatabaseService(
    val webClient: WebClient,
    @Value("\${integrations.dbh.retry.delay.min:100}") val retryMinDelay: Long,
    @Value("\${integrations.dbh.retry.delay.maxy:2000}") val retryMaxDelay: Long,
    @Value("\${integrations.dbh.retry.times:3}") val retryTimes: Long
) {

    suspend fun deleteSchemaById(databaseId: String): JsonNode = request(DELETE, databaseId).awaitFirst()

    suspend fun getSchemaById(databaseId: String): DatabaseResult? =
        request(GET, databaseId).map { it.toDatabaseResponse() }.awaitFirstOrNull()

    private fun request(method: HttpMethod, databaseId: String): Mono<JsonNode> {
        return webClient
            .method(method)
            .uri("/api/v1/schema/{database}", databaseId)
            .retrieve()
            .bodyToMono<JsonNode>()
            .onErrorResume(WebClientResponseException.NotFound::class.java) {
                Mono.empty()
            }
            .retryWithLog(retryMinDelay, retryMaxDelay, retryTimes)
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

        return this.retryWhen(
            Retry.backoff(retryTimes, Duration.ofMillis(retryFirstInMs))
                .maxBackoff(Duration.ofMillis(retryMaxInMs))
                .filter { it !is WebClientResponseException.Unauthorized }
                .doBeforeRetry { logger.debug("retrying message=${it.failure().message}") }
        ).doOnError {
            logger.info {
                val msg = "Retrying failed request, ${it.message}, message=${it.cause?.message}"
                if (it is WebClientResponseException) {
                    "message=$msg, method=${it.request?.method} uri=${it.request?.uri} code=${it.statusCode}"
                } else {
                    msg
                }
            }
        }
    }
}

data class DatabaseResult(val type: String, val id: String, val labels: Map<String, String>)
