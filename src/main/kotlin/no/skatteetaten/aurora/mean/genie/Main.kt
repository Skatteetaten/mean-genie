package no.skatteetaten.aurora.mean.genie

import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.service.DatabaseService
import no.skatteetaten.aurora.mean.genie.service.KubernetesWatcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.event.ContextClosedEvent
import reactor.core.publisher.Mono
import org.springframework.context.event.EventListener

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class Main(
    val watcher: KubernetesWatcher,
    val databaseService: DatabaseService,
    @Value("\${operation.scope:#{null}}") val operationScopeConfiguration: String?
) : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg args: String) {

        val url = "/apis/skatteetaten.no/v1/applicationdeployments?watch=true"
        watcher.watch(url, listOf("DELETED")) {
            val operationScopeLabel = databaseService.readOperationScopeLabel(it)
            val databases = databaseService.readDatabaseLabel(it)

            if (operationScopeLabel == operationScopeConfiguration && databases.isNotEmpty()) {
                databaseService.deleteSchemaByID(databases).then()
                logger.debug { "Deleted schema $databases" }
            }
            Mono.empty()
        }
    }

    @EventListener
    fun handleContextRefreshEvent(ctxStartEvt: ContextClosedEvent) {

        logger.info("SHUTDOWN {}", ctxStartEvt.applicationContext)
    }
}

fun main(args: Array<String>) {

    SpringApplication.run(Main::class.java, *args)
}