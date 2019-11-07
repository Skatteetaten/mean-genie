package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Service
class KubernetesWatcher(val websocketCLient: ReactorNettyWebSocketClient) {

    fun watch(url: String, types: List<String> = emptyList(), fn: (JsonNode) -> Any) {
        var stopped = false
        while (!stopped) {
            logger.debug("Started watch on url={}", url)
            try {
                watchBlocking(url, types, fn)
            } catch (e: Throwable) {
                when (e.cause) {
                    is InterruptedException -> stopped = true
                    else -> logger.error("error occured in watch", e)
                }
            }
        }
    }

    private fun watchBlocking(url: String, types: List<String>, fn: (JsonNode) -> Any) {
        websocketCLient.execute(URI.create(url)) { session ->
            session
                .receive()
                .map { jacksonObjectMapper().readTree(it.payloadAsText) }
                .filter {
                    if (types.isEmpty()) {
                        true
                    } else {
                        it.at("/type").textValue() in types
                    }
                }.flatMap {
                    fn(it)
                    Mono.empty<Void>()
                }.then()
        }.block()
    }
}
