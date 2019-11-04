package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.kubernetesObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Mono
import java.net.URI

private val logger = KotlinLogging.logger {}

@Service
class KubernetesWatcher(val websocketCLient: ReactorNettyWebSocketClient) {

    tailrec fun watch(url: String, types: List<String> = emptyList(), fn: (JsonNode) -> Mono<Void>) {
        logger.info("Started watch on url={}", url)
        try {
            websocketCLient.execute(
                URI.create(url)
            ) { session ->
                session
                    .receive()
                    .map { kubernetesObjectMapper().readTree(it.payloadAsText) }
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
            logger.info("error occured in watch", e)
        } finally {
            logger.info("watcher restarted")
        }
        watch(url, types, fn)
    }
}
