package no.skatteetaten.aurora.mean.genie.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.netty.http.client.PrematureCloseException
import java.net.ConnectException
import java.net.URI

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

    // TODO: Tester vi at denne oppfører seg riktig hvis det kommer nettverksfeil. At den restartes osv?
    // TODO: Jeg tror denne kan erstattes av en retry i metoden watchBlocking. Som retryer på samme condition som catch blokken her
    fun watch(url: String, types: List<String> = emptyList(), fn: suspend (JsonNode) -> Unit) {
        var stopped = false
        while (!stopped) {
            logger.debug("Started watch on url={}", url)
            try {
                watchBlocking(url, types, fn)
            } catch (t: Throwable) {
                stopped = closeableWatcher.stop(t)
                if (!stopped) {
                    logger.error("error occurred in watch", t)
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
                    // TODO: hvis vi skal håndtere å sende med resourceVersion på retry så må vi hente den ut her og sette den en plass, og så må urlen til execute genereres utifra denne informasjonen. F.eks en ConcurrentHashMap fra url til siste revisjon eller noe slikt
                    // TODO: trenger vi å sette noe fra spring i dette scopet? ThreadLocals mdc osv?
                    mono {
                        try {
                            fn(it)
                        } catch (e: Exception) {
                            logger.info("Caught error from watch handle function", e)
                        }
                    }
                }/*.retry { //TODO: Kan denne brukes fremfor koden over? Hvis vi har test på det kan vi sjekke
                   val stopped= closeableWatcher.stop(it)
                    if(!stopped) {
                        logger.error("error occured in watch", it)
                    }
                    stopped
                }*/
                .then()
        }.block()
    }
}
