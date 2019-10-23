package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.fabric8.kubernetes.api.model.HasMetadata
import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.model.WatchEvent
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import java.net.URI

private val logger = KotlinLogging.logger {}

@Service
class KubernetesWatcher(val websocketCLient: ReactorNettyWebSocketClient) {

    // TODO: create url from T
    fun <T : HasMetadata> watch(url: String, fn: (WatchEvent<T>) -> Unit) {

        try {
            websocketCLient.execute(
                URI.create(url)
            ) { session ->
                session.receive()
                    .map { jacksonObjectMapper().readValue<WatchEvent<T>>(it.payloadAsText) }
                    .map {
                        logger.debug("{}", it)
                        // ta vare på siste og bruk den når vi starter opp igjen
                        // it.resource.metadata.resourceVersion
                        fn(it)
                    }
                    .then()
            }
                .block()
        } catch (e: Throwable) {
            logger.info("watcher restarted", e)
            watch(url, fn)
        }
    }
}