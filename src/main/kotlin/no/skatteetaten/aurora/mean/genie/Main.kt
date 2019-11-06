package no.skatteetaten.aurora.mean.genie

import mu.KotlinLogging
import no.skatteetaten.aurora.mean.genie.service.ApplicationDeploymentWatcherService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class Main(
    val adWatcher: ApplicationDeploymentWatcherService
) : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        adWatcher.watch()
    }

    @EventListener
    fun handleContextRefreshEvent(ctxStartEvt: ContextClosedEvent) {

        logger.info("SHUTDOWN {}", ctxStartEvt.applicationContext)
    }
}

fun main(args: Array<String>) {

    SpringApplication.run(Main::class.java, *args)
}
