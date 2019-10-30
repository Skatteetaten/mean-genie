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

    // TODO: create url from T
    fun watch(url: String, types: List<String> = emptyList(), fn: (JsonNode) -> Mono<Void>) {
        try {
            websocketCLient.execute(
                URI.create(url)
            ) { session ->
                session.receive()
                    .map {
                        kubernetesObjectMapper().readTree(it.payloadAsText)
                    }.filter {
                        if (types.isEmpty()) {
                            true
                        } else {
                            it.at("/type").textValue() in types
                        }
                    }.flatMap {
                        logger.debug("{}", it)
                        // ta vare på siste og bruk den når vi starter opp igjen
                        // it.resource.metadata.resourceVersion, kanskje unødvendig når vi reagerer på delete handlinger
                        fn(it)
                    }.then()
            }
                .block()
        } catch (e: Throwable) {
            logger.info("watcher restarted", e)
            watch(url, types, fn)
        }
    }
}
