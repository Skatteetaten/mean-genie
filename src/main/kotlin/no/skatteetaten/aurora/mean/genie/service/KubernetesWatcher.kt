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

    tailrec fun watch(url: String, types: List<String> = emptyList(), fn: (JsonNode) -> Mono<Void>) {
        logger.debug("Started watch on url={}", url)
        try {
            websocketCLient.execute(
                URI.create(url)
            ) { session ->
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
                    }.then()
            }
                .block()
        } catch (e: Throwable) {
            logger.error("error occured in watch", e)
        } finally {
            logger.debug("watcher restarted")
        }
        watch(url, types, fn)
    }
}
