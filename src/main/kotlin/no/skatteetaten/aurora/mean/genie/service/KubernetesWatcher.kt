package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.ConnectException
import java.net.URI
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.netty.http.client.PrematureCloseException

/**
 * The integration test will stop the mock openshift server, which will cause a PrematureCloseException or ConnectException.
 * This needs to be handled and stop the loop in the Watcher code.
 */
@Profile("test")
@Component
class TestCloseableWatcher : CloseableWatcher {
    override fun stop(t: Throwable) =
        t.cause is PrematureCloseException || t.cause?.cause is ConnectException
}

/**
 * A manual shutdown of the application will send either a SIGINT or a SIGTERM, which will cause a InterruptedException.
 * This needs to be handled and stop the loop in the Watcher code.
 */
@Profile("!test")
@Component
class OpenshiftCloseableWatcher : CloseableWatcher {
    override fun stop(t: Throwable) = t.cause is InterruptedException
}

interface CloseableWatcher {
    fun stop(t: Throwable): Boolean
}

private val logger = KotlinLogging.logger {}

@Service
class KubernetesWatcher(
    val websocketClient: ReactorNettyWebSocketClient,
    val closeableWatcher: CloseableWatcher
) {

    fun watch(url: String, types: List<String> = emptyList(), fn: suspend (JsonNode) -> Unit) {
        var stopped = false
        while (!stopped) {
            logger.debug("Started watch on url={}", url)
            try {
                watchBlocking(url, types, fn)
                // This will stop without error after 5 minutes
                logger.debug("Done watching")
            } catch (t: Throwable) {
                stopped = closeableWatcher.stop(t)
                if (!stopped) {
                    logger.warn("error occurred in watch", t)
                } else {
                    logger.debug("Error occured message=${t.localizedMessage} class=${t.javaClass}")
                }
            }
        }
    }

    private fun watchBlocking(url: String, types: List<String>, fn: suspend (JsonNode) -> Unit) {
        websocketClient.execute(URI.create(url)) { session ->
            session.receive()
                .map { jacksonObjectMapper().readTree(it.payloadAsText) }
                .filter {
                    if (types.isEmpty()) {
                        true
                    } else {
                        it.at("/type").textValue() in types
                    }
                }.flatMap {
                    mono {
                        try {
                            fn(it)
                        } catch (e: Exception) {
                            logger.info("Caught error from watch handle function", e)
                        }
                    }
                }
                .then()
        }.block()
    }
}
